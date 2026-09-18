package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.config.*;
import com.cinema.ticketbooking.service.NewsService;
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

@WebMvcTest(NewsController.class)
@ActiveProfiles("test")
@Import({SecurityConfiguration.class, CorsConfig.class, CustomAuthenticationEntryPoint.class})
class NewsControllerSecurityTest {
    @Autowired MockMvc mvc;
    @MockitoBean NewsService service;
    private final String input = """
            {"title":"Test","summary":"Summary","content":"Content","image":"",
             "category":"Tin mới","featured":false,"published":true}
            """;

    @Test
    void anonymousReadersCanReadButCannotCreateUpdateOrDelete() throws Exception {
        mvc.perform(get("/api/v1/news")).andExpect(status().isOk());
        clearInvocations(service);
        mvc.perform(post("/api/v1/news").contentType("application/json").content(input))
                .andExpect(status().is4xxClientError());
        mvc.perform(put("/api/v1/news/1").contentType("application/json").content(input))
                .andExpect(status().is4xxClientError());
        mvc.perform(delete("/api/v1/news/1")).andExpect(status().is4xxClientError());
        verifyNoInteractions(service);
    }

    @Test
    void regularUsersCannotManageNewsAndAdminsCan() throws Exception {
        mvc.perform(delete("/api/v1/news/1").with(user("member").roles("USER")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/admin/news").with(user("member").roles("USER")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
        mvc.perform(post("/api/v1/news").with(user("admin").roles("ADMIN"))
                .contentType("application/json").content(input)).andExpect(status().isCreated());
        mvc.perform(delete("/api/v1/news/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());
        verify(service).save(isNull(), any());
        verify(service).delete(1L);
    }

    @Test
    void invalidCategoryAndBlankContentAreRejected() throws Exception {
        mvc.perform(post("/api/v1/news").with(user("admin").roles("ADMIN"))
                .contentType("application/json").content(input.replace("Tin mới", "Invalid")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/news").with(user("admin").roles("ADMIN"))
                .contentType("application/json").content(input.replace("Content", "")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
