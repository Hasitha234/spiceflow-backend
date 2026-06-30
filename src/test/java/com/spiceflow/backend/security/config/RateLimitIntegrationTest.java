package com.spiceflow.backend.security.config;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@ActiveProfiles("test")
@SpringBootTest(classes = com.spiceflow.backend.SpiceflowBackendApplication.class)
@TestPropertySource(properties = {
        "security.rate-limit.ip.capacity=3",
        "security.rate-limit.ip.refill=3"
})
public class RateLimitIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }


    @Test
    void shouldReturnTooManyRequestsWhenLimitExceeded() throws Exception {
        String json = "{\"email\":\"test@example.com\",\"password\":\"password123\"}";

        // First 3 requests should not be 429 (they will be 401 Unauthorized because of wrong creds, which is fine)
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
                            .header("X-Forwarded-For", "203.0.113.1"))
                    .andExpect(status().isUnauthorized()); // Assuming this IP is valid and passes filter
        }

        // 4th request from the same IP should return 429 Too Many Requests
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("X-Forwarded-For", "203.0.113.1"))
                .andExpect(status().isTooManyRequests());

        // A request from a DIFFERENT IP should not be rate-limited
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("X-Forwarded-For", "203.0.113.2"))
                .andExpect(status().isUnauthorized());
    }
}
