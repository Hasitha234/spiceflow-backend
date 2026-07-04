package com.spiceflow.backend.dashboard.finance.controller;

import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.dashboard.finance.dto.FinanceDashboardResponse;
import com.spiceflow.backend.dashboard.finance.service.FinanceDashboardService;
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
class FinanceDashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FinanceDashboardService service;

    private AuthenticatedUser testUser;

    @BeforeEach
    void setUp() {
        testUser = AuthenticatedUser.builder().id(1L).email("admin@spiceflow.com").tenantId(10L).build();
        FinanceDashboardController controller = new FinanceDashboardController(service);

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
    void should_get_finance_dashboard() throws Exception {
        FinanceDashboardResponse response = new FinanceDashboardResponse(
                new BigDecimal("150000.00"),
                new BigDecimal("80000.00"),
                new BigDecimal("25000.00"),
                new BigDecimal("45000.00"),
                List.of(),
                List.of()
        );

        when(service.getDashboard(eq(10L), anyInt())).thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard/finance?limit=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReceivables").value(150000.00))
                .andExpect(jsonPath("$.totalPayables").value(80000.00))
                .andExpect(jsonPath("$.netCashFlowMonth").value(25000.00))
                .andExpect(jsonPath("$.totalCollectionsMonth").value(45000.00));
    }
}
