package com.spiceflow.backend.sales.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "shop_returns")
public class ShopReturnWorkflowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rep_order_shop_id")
    private @Nullable RepOrderShopWorkflowEntity repOrderShop;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_type")
    private @Nullable String unitType;

    @Column(name = "credit_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal creditValue;

    @Column(name = "return_type", nullable = false)
    private String returnType;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public ShopReturnWorkflowEntity() {
        this.tenantId = 0L;
        this.productId = 0L;
        this.quantity = 0;
        this.creditValue = BigDecimal.ZERO;
        this.returnType = "EXPIRED";
        this.status = "PENDING";
        this.createdAt = Instant.EPOCH;
    }

    public @Nullable Long getId() { return id; }
    public void setId(@Nullable Long id) { this.id = id; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public @Nullable RepOrderShopWorkflowEntity getRepOrderShop() { return repOrderShop; }
    public void setRepOrderShop(@Nullable RepOrderShopWorkflowEntity repOrderShop) { this.repOrderShop = repOrderShop; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public @Nullable String getUnitType() { return unitType; }
    public void setUnitType(@Nullable String unitType) { this.unitType = unitType; }

    public BigDecimal getCreditValue() { return creditValue; }
    public void setCreditValue(BigDecimal creditValue) { this.creditValue = creditValue; }

    public String getReturnType() { return returnType; }
    public void setReturnType(String returnType) { this.returnType = returnType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
