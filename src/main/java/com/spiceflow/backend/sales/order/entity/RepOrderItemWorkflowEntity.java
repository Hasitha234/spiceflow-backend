package com.spiceflow.backend.sales.order.entity;


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
import java.math.BigDecimal;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "rep_order_items")
public class RepOrderItemWorkflowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "rep_order_shop_id", nullable = false)
    private @Nullable RepOrderShopWorkflowEntity repOrderShop;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_type")
    private @Nullable String unitType;

    @Column(name = "rate", nullable = false, precision = 15, scale = 2)
    private BigDecimal rate;

    @Column(name = "gross_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "net_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "is_free_item", nullable = false)
    private Boolean isFreeItem;

    @Column(name = "boxes_needed", nullable = false)
    private Integer boxesNeeded;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public RepOrderItemWorkflowEntity() {
        this.tenantId = 0L;
        this.productId = 0L;
        this.quantity = 0;
        this.rate = BigDecimal.ZERO;
        this.grossAmount = BigDecimal.ZERO;
        this.netAmount = BigDecimal.ZERO;
        this.isFreeItem = false;
        this.boxesNeeded = 0;
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

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }

    public BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }

    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public Boolean getIsFreeItem() { return isFreeItem; }
    public void setIsFreeItem(Boolean isFreeItem) { this.isFreeItem = isFreeItem; }

    public Integer getBoxesNeeded() { return boxesNeeded; }
    public void setBoxesNeeded(Integer boxesNeeded) { this.boxesNeeded = boxesNeeded; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
