package com.spiceflow.backend.sales.loading.entity;


import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "loading_sheet_returns")
public class LoadingSheetReturnWorkflowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "loading_sheet_id", nullable = false)
    private @Nullable LoadingSheetWorkflowEntity loadingSheet;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity_returned", nullable = false)
    private Integer quantityReturned;

    @Column(name = "unit_type")
    private @Nullable String unitType;

    @Column(name = "return_type", nullable = false)
    private String returnType;

    public LoadingSheetReturnWorkflowEntity() {
        this.tenantId = 0L;
        this.productId = 0L;
        this.quantityReturned = 0;
        this.returnType = "DAMAGED";
    }

    public @Nullable Long getId() { return id; }
    public void setId(@Nullable Long id) { this.id = id; }

    public @Nullable LoadingSheetWorkflowEntity getLoadingSheet() { return loadingSheet; }
    public void setLoadingSheet(@Nullable LoadingSheetWorkflowEntity loadingSheet) { this.loadingSheet = loadingSheet; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantityReturned() { return quantityReturned; }
    public void setQuantityReturned(Integer quantityReturned) { this.quantityReturned = quantityReturned; }

    public @Nullable String getUnitType() { return unitType; }
    public void setUnitType(@Nullable String unitType) { this.unitType = unitType; }

    public String getReturnType() { return returnType; }
    public void setReturnType(String returnType) { this.returnType = returnType; }
}
