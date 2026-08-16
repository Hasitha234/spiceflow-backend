package com.spiceflow.backend.sales.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.sales.dto.request.EveningSummaryItemRequest;
import com.spiceflow.backend.sales.dto.request.EveningSummaryRequest;
import com.spiceflow.backend.sales.dto.response.EveningSummaryResponse;
import com.spiceflow.backend.sales.dto.response.StockAvailabilityResponse;
import com.spiceflow.backend.sales.service.EveningSummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.junit.jupiter.api.AfterEach;
import com.spiceflow.backend.common.context.TenantContext;

@ExtendWith(MockitoExtension.class)
class EveningSummaryControllerTest {

    @Mock
    private EveningSummaryService eveningSummaryService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private EveningSummaryRequest request;
    private EveningSummaryResponse response;
    private AuthenticatedUser mockUser;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(1L);
        objectMapper = org.springframework.http.converter.json.Jackson2ObjectMapperBuilder.json()
                .modules(new JavaTimeModule())
                .build();

        EveningSummaryController controller = new EveningSummaryController(eveningSummaryService);

        mockUser = AuthenticatedUser.builder()
                .id(1L)
                .email("admin@test.com")
                .tenantId(1L)
                .build();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(
                    new PageableHandlerMethodArgumentResolver(),
                    new HandlerMethodArgumentResolver() {
                        @Override
                        public boolean supportsParameter(MethodParameter parameter) {
                            return parameter.getParameterType().equals(AuthenticatedUser.class);
                        }

                        @Override
                        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                            return mockUser;
                        }
                    }
                )
                .build();

        request = new EveningSummaryRequest(
                1L, 1L, LocalDate.now(),
                List.of(new EveningSummaryItemRequest(1L, 5, new BigDecimal("100.00"), new BigDecimal("500.00")))
        );

        response = new EveningSummaryResponse(
                1L, 1L, 1L, "Test Rep", 1L, "Test Driver", LocalDate.now(), "ES-001", new BigDecimal("500.00"),
                "PENDING", false, null, null, null, null, null, null, null
        );
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void createEveningSummary() throws Exception {
        when(eveningSummaryService.createEveningSummary(eq(1L), any(EveningSummaryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/sales/evening-summaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.summaryNumber").value("ES-001"));
    }

    @Test
    void updateEveningSummary() throws Exception {
        when(eveningSummaryService.updateEveningSummary(eq(1L), eq(1L), any(EveningSummaryRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/sales/evening-summaries/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    static class CustomPageImpl<T> extends PageImpl<T> {
        public CustomPageImpl(List<T> content) {
            super(content);
        }
        @com.fasterxml.jackson.annotation.JsonIgnore
        @Override
        public Pageable getPageable() { return super.getPageable(); }
        
        @com.fasterxml.jackson.annotation.JsonIgnore
        @Override
        public org.springframework.data.domain.Sort getSort() { return super.getSort(); }
    }

    @Test
    void getEveningSummaries() throws Exception {
        Page<EveningSummaryResponse> page = new CustomPageImpl<>(List.of(response));
        when(eveningSummaryService.getEveningSummaries(eq(1L), any(), any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/sales/evening-summaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void getEveningSummaryById() throws Exception {
        when(eveningSummaryService.getEveningSummaryById(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/sales/evening-summaries/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteEveningSummary() throws Exception {
        mockMvc.perform(delete("/api/v1/sales/evening-summaries/1"))
                .andExpect(status().isNoContent());

        verify(eveningSummaryService).deleteEveningSummary(1L, 1L);
    }

    @Test
    void checkStockAvailability() throws Exception {
        List<StockAvailabilityResponse> stockAvailability = List.of(
                new StockAvailabilityResponse(1L, "Product 1", 5, 10, 0, true)
        );
        when(eveningSummaryService.checkStockAvailability(1L, 1L, 1L)).thenReturn(stockAvailability);

        mockMvc.perform(get("/api/v1/sales/evening-summaries/1/stock-check")
                        .param("warehouseId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].sufficient").value(true));
    }

    @Test
    void proceedEveningSummary() throws Exception {
        mockMvc.perform(post("/api/v1/sales/evening-summaries/1/proceed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"warehouseId\": 1}"))
                .andExpect(status().isOk());

        verify(eveningSummaryService).proceedEveningSummary(1L, 1L, 1L);
    }

    @Test
    void undoProceedEveningSummary() throws Exception {
        mockMvc.perform(post("/api/v1/sales/evening-summaries/1/undo-proceed"))
                .andExpect(status().isOk());

        verify(eveningSummaryService).undoProceedEveningSummary(1L, 1L);
    }
}
