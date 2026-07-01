package com.spiceflow.backend.inventory.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Long warehouseId;
    private String warehouseName;
    private Integer quantityAvailable;
    private Integer quantityReserved;
    private String batchNumber;
    private LocalDate expirationDate;
    private Long version;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
