package com.spiceflow.backend.dashboard.logistics.dto;

public record ActiveLoadingSheetDto(
    Long id,
    String sheetNumber,
    Long driverId,
    String driverName,
    String status,
    String loadingDate,
    int itemCount
) {}
