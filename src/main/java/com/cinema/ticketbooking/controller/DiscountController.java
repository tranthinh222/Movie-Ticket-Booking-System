package com.cinema.ticketbooking.controller;
import com.cinema.ticketbooking.domain.Discount;
import com.cinema.ticketbooking.domain.request.ReqDiscountDto;
import com.cinema.ticketbooking.service.DiscountService;
import com.cinema.ticketbooking.util.SecurityUtil;
import com.cinema.ticketbooking.util.error.IdInvalidException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/v1")
public class DiscountController {
 private final DiscountService service;
 public DiscountController(DiscountService service){this.service=service;}
 @GetMapping("/discounts") public List<Discount> list(){return service.list(false);}
 @PreAuthorize("hasRole('ADMIN')") @GetMapping("/admin/discounts") public List<Discount> admin(){return service.list(true);}
 public record QuoteRequest(@NotBlank @Size(max=40) String code){}
 @PostMapping("/discounts/quote") public DiscountService.Quote quote(@Valid @RequestBody QuoteRequest request){return service.quote(SecurityUtil.getCurrentUserId().orElseThrow(()->new IdInvalidException("Vui lòng đăng nhập.")),request.code());}
 @PreAuthorize("hasRole('ADMIN')") @PostMapping("/discounts") public ResponseEntity<Discount> create(@Valid @RequestBody ReqDiscountDto request){return ResponseEntity.status(HttpStatus.CREATED).body(service.save(null,request));}
 @PreAuthorize("hasRole('ADMIN')") @PutMapping("/discounts/{id}") public Discount update(@PathVariable Long id,@Valid @RequestBody ReqDiscountDto request){return service.save(id,request);}
 @PreAuthorize("hasRole('ADMIN')") @DeleteMapping("/discounts/{id}") public ResponseEntity<Void> delete(@PathVariable Long id){service.delete(id);return ResponseEntity.noContent().build();}
}
