# API Thanh Toán VNPay và Momo

## Tổng quan

Hệ thống đã được tích hợp 2 payment gateway:

- **VNPay**: Cổng thanh toán trực tuyến phổ biến tại Việt Nam
- **Momo**: Ví điện tử Momo

## Cấu hình

### File: `application.properties`

```properties
# VNPay Configuration
vnpay.tmnCode=YOUR_TMN_CODE
vnpay.hashSecret=YOUR_HASH_SECRET
vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.returnUrl=http://localhost:8081/api/v1/payments/vnpay/callback
vnpay.version=2.1.0
vnpay.command=pay

# Momo Configuration
momo.partnerCode=YOUR_PARTNER_CODE
momo.accessKey=YOUR_ACCESS_KEY
momo.secretKey=YOUR_SECRET_KEY
momo.endpoint=https://test-payment.momo.vn/v2/gateway/api/create
momo.returnUrl=http://localhost:8081/api/v1/payments/momo/callback
momo.notifyUrl=http://localhost:8081/api/v1/payments/momo/notify
```

**Lưu ý**: Thay thế các giá trị `YOUR_*` bằng thông tin thực tế từ:

- VNPay: https://sandbox.vnpayment.vn
- Momo: https://developers.momo.vn

## API Endpoints

### 1. Tạo VNPay Payment URL

**POST** `/api/v1/payments/vnpay/create`

**Request Body:**

```json
{
  "bookingId": 1,
  "orderInfo": "Thanh toan ve xem phim",
  "locale": "vn"
}
```

**Response:**

```json
{
  "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...",
  "paymentId": null,
  "message": "Payment URL created successfully"
}
```

**Flow:**

1. Gọi API này với bookingId
2. Nhận được paymentUrl
3. Redirect user đến paymentUrl để thanh toán
4. Sau khi thanh toán, VNPay sẽ redirect về `vnpay.returnUrl`

---

### 2. VNPay Callback (Return URL)

**GET** `/api/v1/payments/vnpay/callback?vnp_ResponseCode=00&vnp_TxnRef=123&...`

**Query Parameters:** (VNPay tự động gửi)

- `vnp_ResponseCode`: Mã kết quả (00 = thành công)
- `vnp_TxnRef`: Payment ID
- `vnp_SecureHash`: Chữ ký bảo mật
- Và nhiều params khác

**Response:**

```
Payment processed successfully
```

**Logic:**

- Kiểm tra chữ ký bảo mật
- Nếu `vnp_ResponseCode = 00`: Cập nhật payment status = PAID
- Nếu khác: Cập nhật payment status = FAILED

---

### 3. Tạo Momo Payment URL

**POST** `/api/v1/payments/momo/create`

**Request Body:**

```json
{
  "bookingId": 1,
  "orderInfo": "Thanh toan ve xem phim"
}
```

**Response:**

```json
{
  "paymentUrl": "https://payment.momo.vn/gw_payment/payment/qr?code=...",
  "paymentId": null,
  "message": "Payment URL created successfully"
}
```

**Flow:**

1. Gọi API này với bookingId
2. Service gửi request đến Momo API để tạo payment
3. Nhận được `payUrl` từ Momo
4. Redirect user đến payUrl để thanh toán
5. Sau khi thanh toán, Momo redirect về `momo.returnUrl`

---

### 4. Momo Callback (Return URL)

**GET** `/api/v1/payments/momo/callback?resultCode=0&orderId=123&...`

**Query Parameters:** (Momo tự động gửi)

- `resultCode`: Mã kết quả (0 = thành công)
- `orderId`: Payment ID
- `signature`: Chữ ký bảo mật
- Và nhiều params khác

**Response:**

```
Payment processed successfully
```

---

### 5. Momo IPN Notification

**POST** `/api/v1/payments/momo/notify`

**Request Body:** (Momo tự động gửi)

```json
{
  "partnerCode": "MOMO",
  "orderId": "123",
  "resultCode": "0",
  "signature": "...",
  ...
}
```

**Response:**

```json
{
  "resultCode": 0,
  "message": "Success"
}
```

**Lưu ý**: Đây là IPN (Instant Payment Notification) từ Momo server, dùng để đảm bảo payment được xử lý ngay cả khi user không quay lại returnUrl.

---

## Luồng Thanh Toán

### VNPay Flow:

```
1. Frontend -> POST /api/v1/payments/vnpay/create {bookingId: 1}
2. Backend tạo Payment record với status = UNPAID
3. Backend tạo VNPay payment URL với HMAC-SHA512 signature
4. Backend trả về paymentUrl
5. Frontend redirect user đến VNPay
6. User thanh toán trên VNPay
7. VNPay redirect về /api/v1/payments/vnpay/callback?vnp_ResponseCode=00&...
8. Backend verify signature và cập nhật Payment status = PAID hoặc FAILED
9. Frontend hiển thị kết quả
```

### Momo Flow:

```
1. Frontend -> POST /api/v1/payments/momo/create {bookingId: 1}
2. Backend tạo Payment record với status = UNPAID
3. Backend gọi Momo API với HMAC-SHA256 signature
4. Momo API trả về payUrl
5. Backend trả về payUrl cho Frontend
6. Frontend redirect user đến Momo
7. User thanh toán trên Momo app/web
8. Momo redirect về /api/v1/payments/momo/callback?resultCode=0&...
9. Backend verify signature và cập nhật Payment status
10. Đồng thời Momo gửi IPN đến /api/v1/payments/momo/notify (backup)
11. Frontend hiển thị kết quả
```

---

## Cấu Trúc Code

### DTOs

- **ReqVNPayPaymentDto**: Request tạo VNPay payment
- **ReqMomoPaymentDto**: Request tạo Momo payment
- **ResPaymentUrlDto**: Response chứa payment URL

### Services

- **VNPayService**: Xử lý logic VNPay

  - `createPaymentUrl()`: Tạo payment URL với signature
  - `verifyCallback()`: Kiểm tra signature callback
  - `processCallback()`: Cập nhật payment status
  - `hmacSHA512()`: Tạo HMAC-SHA512 signature

- **MomoService**: Xử lý logic Momo
  - `createPaymentUrl()`: Gọi Momo API để tạo payment
  - `verifySignature()`: Kiểm tra signature callback
  - `processCallback()`: Cập nhật payment status
  - `hmacSHA256()`: Tạo HMAC-SHA256 signature

### Controller

- **PaymentController**: Expose các API endpoints
  - POST `/payments/vnpay/create`
  - GET `/payments/vnpay/callback`
  - POST `/payments/momo/create`
  - GET `/payments/momo/callback`
  - POST `/payments/momo/notify`

---

## Bảo Mật

### VNPay:

- Sử dụng HMAC-SHA512 để tạo chữ ký
- Verify signature trong callback để đảm bảo request từ VNPay
- Secret key không được expose ra ngoài

### Momo:

- Sử dụng HMAC-SHA256 để tạo chữ ký
- Verify signature trong callback và IPN
- Secret key, access key không được expose

---

## Testing

### VNPay Sandbox:

1. Đăng ký tài khoản sandbox tại https://sandbox.vnpayment.vn
2. Lấy TMN Code và Hash Secret
3. Sử dụng thẻ test:
   - Số thẻ: 9704198526191432198
   - Tên: NGUYEN VAN A
   - Ngày phát hành: 07/15
   - Mật khẩu OTP: 123456

### Momo Test:

1. Đăng ký tài khoản test tại https://developers.momo.vn
2. Lấy Partner Code, Access Key, Secret Key
3. Sử dụng app Momo test để quét QR

---

## Troubleshooting

### VNPay:

- **Invalid signature**: Kiểm tra Hash Secret
- **TMN Code không hợp lệ**: Kiểm tra cấu hình
- **Expired payment**: Default là 15 phút, có thể cấu hình

### Momo:

- **Partner Code không hợp lệ**: Kiểm tra cấu hình
- **Invalid signature**: Kiểm tra Secret Key
- **API timeout**: Momo API có thể chậm trong môi trường test

---

## Production Checklist

- [ ] Thay đổi endpoint từ sandbox/test sang production
- [ ] Cập nhật credentials thật từ VNPay và Momo
- [ ] Cấu hình HTTPS cho returnUrl và notifyUrl
- [ ] Thêm logging cho payment transactions
- [ ] Thêm retry mechanism cho API calls
- [ ] Cấu hình timeout phù hợp
- [ ] Test thoroughly với real payments
- [ ] Backup payment data
