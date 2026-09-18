package com.cinema.ticketbooking.service;
import com.cinema.ticketbooking.domain.Discount;
import com.cinema.ticketbooking.domain.request.ReqDiscountDto;
import com.cinema.ticketbooking.repository.DiscountRepository;
import com.cinema.ticketbooking.util.error.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest @ActiveProfiles("test") @Import(DiscountService.class)
class DiscountServiceIntegrationTest {
 @Autowired DiscountService service;
 @Autowired DiscountRepository repository;
 @MockitoBean SeatHoldService holds;
 private ReqDiscountDto request(String code){
  var r=new ReqDiscountDto();r.setCode(code);r.setTitle("Demo");r.setType(Discount.Type.PERCENT);
  r.setValue(new BigDecimal("10"));r.setMinOrder(BigDecimal.ZERO);r.setMaxDiscount(new BigDecimal("50000"));
  r.setStartsOn(service.today());r.setEndsOn(service.today().plusDays(3));r.setActive(true);return r;
 }
 @Test void calculatesPercentageCapAndFixedDiscountWithoutNegativeTotal(){
  service.save(null,request("cine10"));
  var q=service.calculate(" CINE10 ",new BigDecimal("130000"));assertEquals(0,q.total().compareTo(new BigDecimal("117000")));
  assertEquals(0,service.calculate("CINE10",new BigDecimal("1000000")).discountAmount().compareTo(new BigDecimal("50000")));
  var r=request("FIXED");r.setType(Discount.Type.FIXED);r.setValue(new BigDecimal("20000"));service.save(null,r);
  assertEquals(0,service.calculate("fixed",new BigDecimal("10000")).total().signum());
 }
 @Test void rejectsMinimumOrderExpiredInactiveAndFutureDiscounts(){
  var r=request("MINIMUM");r.setMinOrder(new BigDecimal("150000"));service.save(null,r);
  assertThrows(BadRequestException.class,()->service.calculate("MINIMUM",new BigDecimal("130000")));
  r=request("EXPIRED");r.setStartsOn(service.today().minusDays(2));r.setEndsOn(service.today().minusDays(1));service.save(null,r);
  r=request("FUTURE");r.setStartsOn(service.today().plusDays(1));service.save(null,r);
  r=request("INACTIVE");r.setActive(false);service.save(null,r);
  for(String code:new String[]{"EXPIRED","FUTURE","INACTIVE","MISSING"})assertThrows(BadRequestException.class,()->service.calculate(code,new BigDecimal("130000")));
  assertEquals(1,service.list(false).size());
 }
 @Test void rejectsInvalidConfigurationAndQuoteWithoutHolds(){
  var r=request("INVALID");r.setValue(new BigDecimal("101"));var tooLarge=r;
  assertThrows(BadRequestException.class,()->service.save(null,tooLarge));
  r=request("DATES");r.setEndsOn(service.today().minusDays(1));var dates=r;
  assertThrows(BadRequestException.class,()->service.save(null,dates));
  when(holds.getSeatHoldByUserId(1L)).thenReturn(java.util.List.of());
  assertThrows(BadRequestException.class,()->service.quote(1L,"CINE10"));
 }
 @Test void quoteUsesHeldSeatPricesAndRejectsExpiredHolds(){
  service.save(null,request("CINE10"));
  var variant=new com.cinema.ticketbooking.domain.SeatVariant();variant.setBasePrice(50000);variant.setBonus(10000);
  var seat=new com.cinema.ticketbooking.domain.Seat();seat.setSeatVariant(variant);
  var film=new com.cinema.ticketbooking.domain.Film();film.setPrice(70000L);
  var showtime=new com.cinema.ticketbooking.domain.ShowTime();showtime.setFilm(film);
  var hold=new com.cinema.ticketbooking.domain.SeatHold();hold.setSeat(seat);hold.setShowTime(showtime);hold.setExpiresAt(java.time.Instant.now().plusSeconds(300));
  when(holds.getSeatHoldByUserId(1L)).thenReturn(java.util.List.of(hold));
  var quote=service.quote(1L,"CINE10");
  assertEquals(0,quote.subtotal().compareTo(new BigDecimal("130000")));
  assertEquals(0,quote.total().compareTo(new BigDecimal("117000")));
  hold.setExpiresAt(java.time.Instant.now().minusSeconds(1));
  assertThrows(BadRequestException.class,()->service.quote(1L,"CINE10"));
 }
}
