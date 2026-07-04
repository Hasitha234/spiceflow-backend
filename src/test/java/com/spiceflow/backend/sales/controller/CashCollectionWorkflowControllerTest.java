package com.spiceflow.backend.sales.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.sales.collection.domain.CashCollection;
import com.spiceflow.backend.sales.collection.domain.CashCollectionState;
import com.spiceflow.backend.sales.collection.dto.CashCollectionResponse;
import com.spiceflow.backend.sales.collection.dto.CreateCashCollectionRequest;
import com.spiceflow.backend.sales.collection.service.CashCollectionWorkflowService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CashCollectionWorkflowControllerTest {

    @Mock
    private CashCollectionWorkflowService workflowService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        CashCollectionWorkflowController controller = new CashCollectionWorkflowController(workflowService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().equals(AuthenticatedUser.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        return AuthenticatedUser.builder()
                                .id(1L)
                                .email("admin@spiceflow.com")
                                .tenantId(10L)
                                .authorities(List.of())
                                .build();
                    }
                })
                .build();
    }

    @Test
    void should_create_collection_successfully() throws Exception {
        CreateCashCollectionRequest request = CreateCashCollectionRequest.builder()
                .shopId(100L)
                .repId(5L)
                .collectionDate(LocalDate.of(2026, 7, 4))
                .amount(BigDecimal.valueOf(5000))
                .paymentMethod("CASH")
                .notes("Payment received")
                .build();

        CashCollection created = CashCollection.create("COL-12345678", 10L, 100L, 5L,
                LocalDate.of(2026, 7, 4), BigDecimal.valueOf(5000), "CASH", null, null, null, "Payment received", "admin@spiceflow.com");

        when(workflowService.createCollection(eq(10L), any(CreateCashCollectionRequest.class), eq("admin@spiceflow.com")))
                .thenReturn(CashCollectionResponse.from(created));

        mockMvc.perform(post("/api/v1/sales/collections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.collectionNumber").value("COL-12345678"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.amount").value(5000));
    }

    @Test
    void should_confirm_collection_successfully() throws Exception {
        CashCollection confirmed = new CashCollection(
                CashCollection.create("COL-12345678", 10L, 100L, 5L,
                        LocalDate.of(2026, 7, 4), BigDecimal.valueOf(5000), "CASH", null, null, null, "Note", "admin"),
                CashCollectionState.CONFIRMED
        );

        when(workflowService.confirmCollection(eq("COL-12345678"), eq(10L), eq(1L), eq("Verified")))
                .thenReturn(CashCollectionResponse.from(confirmed));

        mockMvc.perform(post("/api/v1/sales/collections/COL-12345678/confirm?comment=Verified"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void should_list_collections_successfully() throws Exception {
        CashCollection pending = CashCollection.create("COL-12345678", 10L, 100L, 5L,
                LocalDate.of(2026, 7, 4), BigDecimal.valueOf(5000), "CASH", null, null, null, "Note", "admin");

        when(workflowService.listCollections(10L, null)).thenReturn(List.of(pending));

        mockMvc.perform(get("/api/v1/sales/collections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].collectionNumber").value("COL-12345678"));
    }
}
