package com.spiceflow.backend.sales.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record BillRequest(
        @NotNull(message = "Rep ID is required")
        Long repId,

        @org.jspecify.annotations.Nullable Long driverId,

        @NotNull(message = "Shop ID is required")
        Long shopId,

        @NotNull(message = "Bill Date is required")
        LocalDate billDate,

        @NotNull(message = "Net Total is required")
        @Min(value = 0, message = "Net Total cannot be negative")
        BigDecimal netTotal,

        @org.jspecify.annotations.Nullable
        @Min(value = 0, message = "Reverse GRTs cannot be negative")
        BigDecimal reverseGrts,

        @NotNull(message = "Free Items Value is required")
        @Min(value = 0, message = "Free Items Value cannot be negative")
        BigDecimal freeItemsValue,

        @NotNull(message = "Discount is required")
        @Min(value = 0, message = "Discount cannot be negative")
        BigDecimal discount,

        @NotNull(message = "SKU Discount is required")
        @Min(value = 0, message = "SKU Discount cannot be negative")
        BigDecimal skuDiscount,

        @NotNull(message = "Return Amount is required")
        @Min(value = 0, message = "Return Amount cannot be negative")
        BigDecimal returnAmount
) {}
