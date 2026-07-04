package com.spiceflow.backend.dashboard.purchasing.dto;

public record SupplierLeadTimeDto(
    Long supplierId,
    String supplierName,
    long totalOrders,
    long completedOrders,
    double averageLeadTimeDays
) {}
