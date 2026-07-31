package com.spiceflow.backend.sales.dto.request;

import jakarta.validation.constraints.NotNull;

public record DeductInventoryRequest(
    @NotNull(message = "Warehouse ID is required") Long warehouseId
) {}
