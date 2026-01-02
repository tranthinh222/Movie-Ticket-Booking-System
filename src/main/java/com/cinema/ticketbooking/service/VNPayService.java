package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Booking;
import com.cinema.ticketbooking.domain.Payment;
import com.cinema.ticketbooking.util.constant.BookingStatusEnum;
import com.cinema.ticketbooking.util.constant.PaymentMethodEnum;
import com.cinema.ticketbooking.util.constant.PaymentStatusEnum;
import com.cinema.ticketbooking.util.error.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {
    @Value("${vnpay.tmnCode}")
    private String vnpTmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnpHashSecret;

    @Value("${vnpay.url}")
    private String vnpUrl;

    @Value("${vnpay.returnUrl}")
    private String vnpReturnUrl;

    @Value("${vnpay.version}")
    private String vnpVersion;

    @Value("${vnpay.command}")
    private String vnpCommand;

    @Value("${vnpay.directUrlSuccess}")
    private String directUrlSuccess;

    @Value("${vnpay.directUrlError}")
    private String directUrlError;

    private final PaymentService paymentService;
    private final BookingService bookingService;
    private final EmailService emailService;
    private final QRCodeService qrCodeService;

    public VNPayService(PaymentService paymentService, BookingService bookingService,
            EmailService emailService, QRCodeService qrCodeService) {
        this.paymentService = paymentService;
        this.bookingService = bookingService;
        this.emailService = emailService;
        this.qrCodeService = qrCodeService;
    }

    public String createPaymentUrl(Long paymentId, Double price, String orderInfo, String ipAddress)
            throws UnsupportedEncodingException {
        Payment payment = paymentService.getPaymentById(paymentId);
        if (payment == null) {
            throw new BadRequestException("Payment not found");
        }

        // Generate UUID for transaction reference if not exists
        if (payment.getTransactionRef() == null || payment.getTransactionRef().isEmpty()) {
            String transactionRef = UUID.randomUUID().toString();
            payment.setTransactionRef(transactionRef);
            paymentService.savePayment(payment);
        }

        // Calculate amount (VNPay requires amount in VND * 100)
        long amount = (long) (price * 100);

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnpVersion);
        vnpParams.put("vnp_Command", vnpCommand);
        vnpParams.put("vnp_TmnCode", vnpTmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", payment.getTransactionRef()); // Use UUID instead of payment ID
        vnpParams.put("vnp_OrderInfo",
                orderInfo != null ? orderInfo : "Thanh toan ve xem phim #" + payment.getBooking().getId());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnpReturnUrl);
        vnpParams.put("vnp_IpAddr", ipAddress);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnpExpireDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);

        // Build hash data and query string
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data - URL ENCODE the value
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));

                // Build query - Same as hash data
                query.append(fieldName);
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnpSecureHash = hmacSHA512(vnpHashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnpUrl + "?" + queryUrl;

        // Debug logging
        System.out.println("=== VNPay Payment URL Debug ===");
        System.out.println("Hash Data: " + hashData.toString());
        System.out.println("Secure Hash: " + vnpSecureHash);
        System.out.println("Payment URL: " + paymentUrl);
        System.out.println("================================");

        return paymentUrl;
    }

    public boolean verifyCallback(Map<String, String> params) {
        String vnpSecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHashType");
        params.remove("vnp_SecureHash");

        // Build hash data
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // URL ENCODE for hash verification (same as when creating payment URL)
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        String calculatedHash = hmacSHA512(vnpHashSecret, hashData.toString());

        // Debug logging
        System.out.println("=== VNPay Callback Verification ===");
        System.out.println("Hash Data: " + hashData.toString());
        System.out.println("Calculated Hash: " + calculatedHash);
        System.out.println("Received Hash: " + vnpSecureHash);
        System.out.println("Match: " + calculatedHash.equals(vnpSecureHash));
        System.out.println("===================================");

        return calculatedHash.equals(vnpSecureHash);
    }

    public String processCallback(Map<String, String> params) {
        if (!verifyCallback(params)) {
            return directUrlError + "?message=Invalid+signature";
        }

        String responseCode = params.get("vnp_ResponseCode");
        String transactionRef = params.get("vnp_TxnRef"); // UUID instead of payment ID

        // Get payment by transaction reference
        Payment payment = paymentService.getPaymentByTransactionRef(transactionRef);

        if ("00".equals(responseCode)) {
            // Payment successful
            paymentService.updatePaymentStatus(payment.getId(), PaymentStatusEnum.PAID);

            // Generate QR code for booking
            Booking booking = payment.getBooking();
            if (booking != null) {
                // Update booking status to CONFIRMED
                booking.setStatus(BookingStatusEnum.CONFIRMED);

                // Generate QR code if not exists
                if (booking.getQrCode() == null || booking.getQrCode().isEmpty()) {
                    String qrCode = qrCodeService.generateBookingQRCodeWithDetails(
                            booking.getId(),
                            booking.getUser().getEmail(),
                            booking.getTotal_price());
                    booking.setQrCode(qrCode);
                }

                bookingService.updateBooking(booking);

                // Send email with QR code
                emailService.sendBookingConfirmationWithQR(booking);
            }
            return directUrlSuccess + "?paymentId=" + payment.getId() + "&bookingId=" + booking.getId();
        } else {
            // Payment failed - delete booking, booking items and payment
            if (payment != null && payment.getBooking() != null) {
                Long bookingId = payment.getBooking().getId();
                // Delete booking (cascade will delete booking items and payment)
                bookingService.deleteBooking(bookingId);
            }
            return directUrlError + "?paymentId=" + payment.getId() + "&responseCode=" + responseCode;
        }
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error while generating HMAC SHA512", ex);
        }
    }
}
