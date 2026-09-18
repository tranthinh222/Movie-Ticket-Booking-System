package com.cinema.ticketbooking.service;
import com.cinema.ticketbooking.domain.Discount;
import com.cinema.ticketbooking.domain.request.ReqDiscountDto;
import com.cinema.ticketbooking.repository.DiscountRepository;
import com.cinema.ticketbooking.util.error.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.*;
import java.util.*;
@Service
public class DiscountService {
 private final DiscountRepository repository;
 private final SeatHoldService holds;
 public DiscountService(DiscountRepository repository,SeatHoldService holds){this.repository=repository;this.holds=holds;}
 public LocalDate today(){return LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));}
 public List<Discount> list(boolean admin){return admin?repository.findAll(org.springframework.data.domain.Sort.by("id").descending()):repository.findByActiveTrueAndStartsOnLessThanEqualAndEndsOnGreaterThanEqualOrderByIdDesc(today(),today());}
 public record Quote(String code,BigDecimal subtotal,BigDecimal discountAmount,BigDecimal total){}
 public Quote calculate(String code,BigDecimal subtotal){
  if(subtotal==null||subtotal.signum()<=0)throw new BadRequestException("Đơn đặt vé không có giá trị hợp lệ.");
  var discount=repository.findByCode(code.trim().toUpperCase(Locale.ROOT)).orElseThrow(()->new BadRequestException("Mã ưu đãi không tồn tại."));
  if(!discount.isActive())throw new BadRequestException("Mã ưu đãi đã ngừng áp dụng.");
  if(today().isBefore(discount.getStartsOn()))throw new BadRequestException("Mã ưu đãi chưa đến ngày áp dụng.");
  if(today().isAfter(discount.getEndsOn()))throw new BadRequestException("Mã ưu đãi đã hết hạn.");
  if(subtotal.compareTo(discount.getMinOrder())<0)throw new BadRequestException("Đơn cần tối thiểu "+discount.getMinOrder().toPlainString()+"đ để áp dụng mã này.");
  var amount=discount.getType()==Discount.Type.PERCENT?subtotal.multiply(discount.getValue()).divide(new BigDecimal("100")):discount.getValue();
  if(discount.getMaxDiscount()!=null)amount=amount.min(discount.getMaxDiscount());
  amount=amount.min(subtotal).setScale(0,RoundingMode.DOWN);
  return new Quote(discount.getCode(),subtotal,amount,subtotal.subtract(amount));
 }
 @Transactional(readOnly=true)
 public Quote quote(Long userId,String code){
  var items=holds.getSeatHoldByUserId(userId);
  if(items.isEmpty()||items.stream().anyMatch(h->h.getExpiresAt()==null||!h.getExpiresAt().isAfter(Instant.now())))throw new BadRequestException("Thời gian giữ ghế đã hết. Vui lòng chọn ghế lại.");
  var total=items.stream().map(h->BigDecimal.valueOf(h.getSeat().getSeatVariant().getBasePrice()).add(BigDecimal.valueOf(h.getSeat().getSeatVariant().getBonus())).add(BigDecimal.valueOf(h.getShowTime().getFilm().getPrice()))).reduce(BigDecimal.ZERO,BigDecimal::add);
  return calculate(code,total);
 }
 @Transactional public Discount save(Long id,ReqDiscountDto request){
  if(request.getEndsOn().isBefore(request.getStartsOn()))throw new BadRequestException("Ngày kết thúc phải từ ngày bắt đầu trở đi.");
  if(request.getType()==Discount.Type.PERCENT&&request.getValue().compareTo(new BigDecimal("100"))>0)throw new BadRequestException("Phần trăm giảm không được vượt quá 100%.");
  String code=request.getCode().trim().toUpperCase(Locale.ROOT);
  var duplicate=repository.findByCode(code);
  if(duplicate.isPresent()&&!duplicate.get().getId().equals(id))throw new ResourceAlreadyExistsException("Mã ưu đãi đã tồn tại.");
  var item=id==null?new Discount():get(id);
  item.setCode(code);item.setTitle(request.getTitle().trim());item.setDescription(request.getDescription());item.setImage(request.getImage());item.setType(request.getType());item.setValue(request.getValue());item.setMinOrder(request.getMinOrder());item.setMaxDiscount(request.getMaxDiscount());item.setStartsOn(request.getStartsOn());item.setEndsOn(request.getEndsOn());item.setActive(request.isActive());
  return repository.save(item);
 }
 public Discount get(Long id){return repository.findById(id).orElseThrow(()->new IdInvalidException("Không tìm thấy ưu đãi."));}
 @Transactional public void delete(Long id){repository.delete(get(id));}
}
