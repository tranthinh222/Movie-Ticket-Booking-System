package com.cinema.ticketbooking.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.cors.DefaultCorsProcessor;
import static org.junit.jupiter.api.Assertions.*;

class CorsConfigTest {
    @Test
    void vercelOriginReceivesCorsHeadersForGetAndPreflight() throws java.io.IOException {
        String origin = "https://movie-ticket-booking-frontend-gamma.vercel.app";
        for (String method : new String[] { "GET", "OPTIONS" }) {
            var request = new MockHttpServletRequest(method, "/api/v1/films");
            request.addHeader("Origin", origin);
            if (method.equals("OPTIONS")) {
                request.addHeader("Access-Control-Request-Method", "GET");
                request.addHeader("Access-Control-Request-Headers", "authorization,content-type");
            }
            var response = new MockHttpServletResponse();
            assertTrue(new DefaultCorsProcessor().processRequest(
                    new CorsConfig().corsConfigurationSource().getCorsConfiguration(request), request, response));
            assertEquals(origin, response.getHeader("Access-Control-Allow-Origin"));
        }
    }
}
