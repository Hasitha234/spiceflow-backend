package com.spiceflow.backend.inventory.dto.response;
import lombok.Builder;

import com.spiceflow.backend.inventory.entity.Supplier;
import java.time.OffsetDateTime;

@Builder
public record SupplierResponse(
    Long id,
    String name,
    String contactEmail,
    String contactPhone,
    String address,
    String taxId,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static SupplierResponse fromEntity(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getContactEmail(),
                supplier.getContactPhone(),
                supplier.getAddress(),
                supplier.getTaxId(),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt()
        );
    }
}