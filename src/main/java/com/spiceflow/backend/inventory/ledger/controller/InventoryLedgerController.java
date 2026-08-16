package com.spiceflow.backend.inventory.ledger.controller;

import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.common.dto.PageResponse;
import com.spiceflow.backend.inventory.dto.response.TransferHistoryResponse;
import com.spiceflow.backend.inventory.ledger.service.InventoryLedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory/ledger")
@Tag(name = "Inventory Ledger", description = "Inventory Ledger APIs")
public class InventoryLedgerController {

    private final InventoryLedgerService ledgerService;

    public InventoryLedgerController(InventoryLedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping("/transfers")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    @Operation(summary = "List transfer history", description = "Get paginated list of inventory transfers", operationId = "listTransfers")
    public ResponseEntity<PageResponse<TransferHistoryResponse>> listTransfers(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long tenantId = Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        
        Page<TransferHistoryResponse> resultPage = ledgerService.listTransfers(tenantId, warehouseId, startDate, endDate, pageable);
        return ResponseEntity.ok(PageResponse.of(resultPage));
    }
}
