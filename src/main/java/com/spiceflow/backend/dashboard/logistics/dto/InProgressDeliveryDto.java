package com.spiceflow.backend.dashboard.logistics.dto;

public record InProgressDeliveryDto(
    Long id,
    String deliveryNumber,
    String loadingSheetNumber,
    String driverName,
    String status,
    String deliveryDate,
    int shopCount
) {}
