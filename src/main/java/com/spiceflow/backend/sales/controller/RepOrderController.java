package com.spiceflow.backend.sales.controller;

import org.springframework.validation.annotation.Validated;
import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.sales.dto.request.CreateRepOrderRequest;
import com.spiceflow.backend.sales.dto.response.RepOrderResponse;
import com.spiceflow.backend.sales.service.RepOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Validated
@RequestMapping("/api/v1/sales/rep-orders")
@RequiredArgsConstructor
@Tag(name = "Rep Orders", description = "Endpoints for managing rep orders and shop returns")
public class RepOrderController {

    private final RepOrderService repOrderService;

    @PostMapping
    @PreAuthorize("hasAuthority('REP_ORDER_CREATE')")
    @Operation(summary = "Create a rep order", description = "Creates a rep order with multiple shops and line items", operationId = "createRepOrder")
    public ResponseEntity<RepOrderResponse> createRepOrder(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreateRepOrderRequest request) {
        log.info("User {} creating rep order", currentUser.getId());
        RepOrderResponse response = repOrderService.createRepOrder(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REP_ORDER_VIEW')")
    @Operation(summary = "List rep orders", description = "Returns a paginated list of rep orders", operationId = "getRepOrders")
    public ResponseEntity<Page<RepOrderResponse>> getRepOrders(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) Long repId,
            Pageable pageable) {
        log.info("User {} listing rep orders", currentUser.getId());
        Page<RepOrderResponse> response = repOrderService.getRepOrders(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), repId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('REP_ORDER_VIEW')")
    @Operation(summary = "Get rep order by ID", description = "Returns details of a specific rep order", operationId = "getRepOrder")
    public ResponseEntity<RepOrderResponse> getRepOrder(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        log.info("User {} getting rep order {}", currentUser.getId(), id);
        RepOrderResponse response = repOrderService.getRepOrder(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"));
        return ResponseEntity.ok(response);
    }
}


