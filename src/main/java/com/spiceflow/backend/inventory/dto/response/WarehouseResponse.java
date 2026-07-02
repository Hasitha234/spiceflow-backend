package com.spiceflow.backend.inventory.dto.response;

import com.spiceflow.backend.inventory.entity.Warehouse;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WarehouseResponse {
    private Long id;
    private String name;
    private String location;
    private Integer capacity;
    private String storeType;
    private Boolean isSystemStore;
    private String description;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static WarehouseResponse fromEntity(Warehouse warehouse) {
        return WarehouseResponse.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .location(warehouse.getLocation())
                .capacity(warehouse.getCapacity())
                .storeType(warehouse.getStoreType())
                .isSystemStore(warehouse.getIsSystemStore())
                .description(warehouse.getDescription())
                .createdAt(warehouse.getCreatedAt())
                .updatedAt(warehouse.getUpdatedAt())
                .build();
    }
}
