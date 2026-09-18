package com.cinema.ticketbooking.controller;
import com.cinema.ticketbooking.config.*;
import com.cinema.ticketbooking.service.DiscountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(DiscountController.class) @ActiveProfiles("test")
@Import({SecurityConfiguration.class,CorsConfig.class,CustomAuthenticationEntryPoint.class})
class DiscountSecurityTest {
 @Autowired MockMvc mvc;
 @MockitoBean DiscountService service;
 @Test void publicReadWorksButQuotesAndManagementRequireAuthentication() throws Exception {
  mvc.perform(get("/api/v1/discounts")).andExpect(status().isOk());
  clearInvocations(service);
  mvc.perform(post("/api/v1/discounts/quote").contentType("application/json").content("{\"code\":\"CINE10\"}")).andExpect(status().isUnauthorized());
  mvc.perform(delete("/api/v1/discounts/1")).andExpect(status().isUnauthorized());
  verifyNoInteractions(service);
 }
 @Test void onlyAdminsCanManageDiscounts() throws Exception {
  mvc.perform(get("/api/v1/admin/discounts").with(user("member").roles("CUSTOMER"))).andExpect(status().isForbidden());
  mvc.perform(delete("/api/v1/discounts/1").with(user("member").roles("CUSTOMER"))).andExpect(status().isForbidden());
  verifyNoInteractions(service);
  mvc.perform(delete("/api/v1/discounts/1").with(user("admin").roles("ADMIN"))).andExpect(status().isNoContent());
  verify(service).delete(1L);
 }
}
