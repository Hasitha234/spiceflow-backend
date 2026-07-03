package com.spiceflow.backend.sales.entity;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.inventory.entity.Product;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@org.hibernate.annotations.Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Table(name = "loading_sheet_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoadingSheetItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loading_sheet_id", nullable = false)
    private LoadingSheet loadingSheet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity_loaded", nullable = false)
    @Builder.Default
    private Integer quantityLoaded = 0;

    @Column(name = "unit_type", length = 10)
    private String unitType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
