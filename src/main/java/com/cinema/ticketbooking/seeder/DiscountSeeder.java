package com.cinema.ticketbooking.seeder;
import com.cinema.ticketbooking.domain.Discount;
import com.cinema.ticketbooking.repository.DiscountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
@Component @Profile("local") @Order(8)
public class DiscountSeeder implements CommandLineRunner {
 private final DiscountRepository repository;
 public DiscountSeeder(DiscountRepository repository){this.repository=repository;}
 @Override @Transactional public void run(String...args){
  var today=LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
  String[] codes={"CINE10","GIAM20K","NHOM15"};
  String[] descriptions={
   "Tận hưởng ưu đãi giảm 10% giá trị đơn vé, tối đa 50.000đ. Nhập mã CINE10 tại bước thanh toán để nhận ưu đãi.",
   "Tiết kiệm 20.000đ khi đặt vé với giá trị đơn từ 150.000đ. Áp dụng mã GIAM20K tại bước thanh toán.",
   "Cùng bạn bè tận hưởng ưu đãi giảm 15% cho đơn vé từ 300.000đ, tối đa 100.000đ. Nhập mã NHOM15 khi thanh toán."
  };
  for(int i=0;i<codes.length;i++){
   String key="discount-demo-"+i;
   var existing=repository.findBySeedKey(key);
   if(existing.isPresent()){
    var discount=existing.get();
    if("Ưu đãi demo local. Mỗi đơn áp dụng một mã; kiểm tra điều kiện trước khi xác nhận.".equals(discount.getDescription())){
     discount.setDescription(descriptions[i]);repository.save(discount);
    }
    continue;
   }
   if(repository.findByCode(codes[i]).isPresent())continue;
   var d=new Discount();d.setSeedKey(key);d.setCode(codes[i]);d.setTitle(new String[]{"Giảm 10% vé phim","Giảm 20.000đ cho đơn từ 150.000đ","Giảm 15% cho đơn từ 300.000đ"}[i]);
   d.setDescription(descriptions[i]);d.setType(i==1?Discount.Type.FIXED:Discount.Type.PERCENT);d.setValue(new BigDecimal(i==1?"20000":i==0?"10":"15"));d.setMinOrder(new BigDecimal(i==0?"0":i==1?"150000":"300000"));d.setMaxDiscount(new BigDecimal(i==0?"50000":i==1?"20000":"100000"));d.setStartsOn(today);d.setEndsOn(today.plusDays(365));d.setActive(true);repository.save(d);
  }
 }
}
