package com.spiceflow.backend.dashboard.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.dashboard.inventory.dto.InventoryDashboardResponse;
import com.spiceflow.backend.dashboard.inventory.service.InventoryDashboardService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InventoryDashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InventoryDashboardService service;

    private AuthenticatedUser testUser;

    @BeforeEach
    void setUp() {
        testUser = AuthenticatedUser.builder().id(1L).email("admin@spiceflow.com").tenantId(10L).build();
        InventoryDashboardController controller = new InventoryDashboardController(service);

        HandlerMethodArgumentResolver principalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().equals(AuthenticatedUser.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return testUser;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(principalResolver)
                .build();
    }

    @Test
    void should_get_inventory_dashboard() throws Exception {
        InventoryDashboardResponse response = new InventoryDashboardResponse(
                new BigDecimal("25000.00"),
                50L,
                3L,
                1L,
                List.of(),
                List.of(),
                List.of()
        );

        when(service.getDashboard(eq(10L), anyInt())).thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard/inventory?limit=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStockValue").value(25000.00))
                .andExpect(jsonPath("$.totalItemsCount").value(50))
                .andExpect(jsonPath("$.lowStockCount").value(3))
                .andExpect(jsonPath("$.pendingTransfersCount").value(1));
    }
}
