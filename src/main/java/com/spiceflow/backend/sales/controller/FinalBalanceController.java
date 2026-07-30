package com.spiceflow.backend.sales.controller;

import com.spiceflow.backend.sales.dto.request.FinalBalanceRequest;
import com.spiceflow.backend.sales.dto.response.FinalBalanceCalculationResponse;
import com.spiceflow.backend.sales.dto.response.FinalBalanceResponse;
import com.spiceflow.backend.sales.service.FinalBalanceService;
import com.spiceflow.backend.common.context.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/final-balances")
@RequiredArgsConstructor
@Tag(name = "Final Balances", description = "Final Balance Reconciliation API")
public class FinalBalanceController {

    private final FinalBalanceService finalBalanceService;

    @GetMapping
    @Operation(summary = "Get final balances with filtering and pagination")
    public Page<FinalBalanceResponse> getFinalBalances(
            @RequestParam(required = false) Long repId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate balanceDate,
            Pageable pageable) {
        return finalBalanceService.getFinalBalances(TenantContext.getTenantId(), repId, balanceDate, pageable);
    }

    @PostMapping("/calculate")
    @Operation(summary = "Preview final balance calculation")
    public FinalBalanceCalculationResponse calculateBalance(@Valid @RequestBody FinalBalanceRequest request) {
        return finalBalanceService.calculateBalance(TenantContext.getTenantId(), request);
    }

    @PostMapping
    @Operation(summary = "Save final balance")
    @ResponseStatus(HttpStatus.CREATED)
    public FinalBalanceResponse saveFinalBalance(@Valid @RequestBody FinalBalanceRequest request) {
        return finalBalanceService.saveFinalBalance(TenantContext.getTenantId(), request);
    }
}
