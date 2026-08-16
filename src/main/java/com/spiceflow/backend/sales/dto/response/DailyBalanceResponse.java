package com.spiceflow.backend.sales.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyBalanceResponse {
    private LocalDate date;
    private BigDecimal morningSummaryTotal;
    private BigDecimal cancelSummaryTotal;
    private BigDecimal eveningSummaryTotal;
    private BigDecimal netDispatchTotal;
    private BigDecimal billsTotal;
    @JsonProperty("isBalanced")
    private boolean isBalanced;
    private String status;
}
