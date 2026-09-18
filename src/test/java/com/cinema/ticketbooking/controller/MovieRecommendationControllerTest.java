package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.config.*;
import com.cinema.ticketbooking.service.MovieRecommendationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({MovieRecommendationController.class,SeatRecommendationController.class})
@ActiveProfiles("test")
@Import({ SecurityConfiguration.class, CorsConfig.class, CustomAuthenticationEntryPoint.class })
class MovieRecommendationControllerTest {
    @Autowired
    MockMvc mvc;
    @MockitoBean
    MovieRecommendationService service;
    @MockitoBean
    com.cinema.ticketbooking.service.SeatRecommendationService seatService;

    @Test
    void anonymousCanRecommend() throws Exception {
        mvc.perform(post("/api/v1/assistant/recommendations").contentType("application/json").content("{}"))
                .andExpect(status().isOk());
        verify(service).recommend(any());
    }

    @Test
    void invalidDurationIsRejected() throws Exception {
        mvc.perform(post("/api/v1/assistant/recommendations").contentType("application/json")
                .content("{\"maxDuration\":0}")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
    @Test void anonymousCanRequestSeatsAndInvalidGroupIsRejected() throws Exception {
      mvc.perform(post("/api/v1/assistant/seat-recommendations").contentType("application/json").content("{\"showTimeId\":1,\"people\":2}")).andExpect(status().isOk());
      verify(seatService).recommend(any());clearInvocations(seatService);
      mvc.perform(post("/api/v1/assistant/seat-recommendations").contentType("application/json").content("{\"showTimeId\":1,\"people\":0}")).andExpect(status().isBadRequest());verifyNoInteractions(seatService);
    }
}
