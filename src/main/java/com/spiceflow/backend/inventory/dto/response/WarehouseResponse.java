package com.spiceflow.backend.inventory.dto.response;
import lombok.Builder;

import com.spiceflow.backend.inventory.entity.Warehouse;
import java.time.OffsetDateTime;

@Builder
public record WarehouseResponse(
    Long id,
    String name,
    String location,
    Integer capacity,
    String storeType,
    Boolean isSystemStore,
    String description,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static WarehouseResponse fromEntity(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getName(),
                warehouse.getLocation(),
                warehouse.getCapacity(),
                warehouse.getStoreType(),
                warehouse.getIsSystemStore(),
                warehouse.getDescription(),
                warehouse.getCreatedAt(),
                warehouse.getUpdatedAt()
        );
    }
}