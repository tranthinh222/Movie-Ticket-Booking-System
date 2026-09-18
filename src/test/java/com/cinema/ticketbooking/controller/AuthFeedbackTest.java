package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.config.*;
import com.cinema.ticketbooking.service.AuthService;
import com.cinema.ticketbooking.util.error.DuplicateEmailException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@Import({SecurityConfiguration.class, CorsConfig.class, CustomAuthenticationEntryPoint.class})
class AuthFeedbackTest {
    @Autowired MockMvc mvc;
    @MockitoBean AuthService service;
    @MockitoBean com.cinema.ticketbooking.service.UserService userService;

    @Test
    void incorrectCredentialsReturnClearMessageWithoutInternalDetails() throws Exception {
        when(service.login(any())).thenThrow(new BadCredentialsException("Bad credentials"));
        mvc.perform(post("/api/v1/auth/login").contentType("application/json")
                .content("{\"email\":\"test@example.com\",\"password\":\"incorrect\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email hoặc mật khẩu không đúng. Vui lòng kiểm tra lại."));
    }

    @Test
    void duplicateEmailReturnsActionableMessage() throws Exception {
        String explanation = "Email này đã được đăng ký. Vui lòng đăng nhập hoặc dùng email khác.";
        when(service.register(any())).thenThrow(new DuplicateEmailException(explanation));
        mvc.perform(post("/api/v1/auth/register").contentType("application/json")
                .content("{\"email\":\"test@example.com\",\"password\":\"123456\",\"username\":\"Test\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value(explanation));
    }

    @Test
    void invalidRegistrationReportsErrorsPerField() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType("application/json")
                .content("{\"email\":\"invalid\",\"password\":\"123\",\"username\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isArray())
                .andExpect(jsonPath("$.data.email[0]").value("Email không đúng định dạng."))
                .andExpect(jsonPath("$.data.password[0]").value("Mật khẩu phải có ít nhất 6 ký tự."))
                .andExpect(jsonPath("$.data.username[0]").value("Vui lòng nhập tên người dùng."));
        verifyNoInteractions(service);
    }
}
