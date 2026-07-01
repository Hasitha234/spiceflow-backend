package com.spiceflow.backend.inventory.dto.response;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryTransactionResponse {
    private Long id;
    private Long inventoryItemId;
    private String productName;
    private String warehouseName;
    private String transactionType;
    private Integer quantity;
    private String referenceId;
    private String notes;
    private OffsetDateTime createdAt;
}
