package com.spiceflow.backend.sales.order.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "rep_order_shops")
public class RepOrderShopWorkflowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rep_order_id", nullable = false)
    private @Nullable RepOrderWorkflowEntity repOrder;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "gross_order_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal grossOrderAmount;

    @Column(name = "returns_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal returnsValue;

    @Column(name = "net_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(
        mappedBy = "repOrderShop",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<RepOrderItemWorkflowEntity> items = new ArrayList<>();

    @OneToMany(
        mappedBy = "repOrderShop",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<ShopReturnWorkflowEntity> returns = new ArrayList<>();

    public RepOrderShopWorkflowEntity() {
        this.tenantId = 0L;
        this.shopId = 0L;
        this.grossOrderAmount = BigDecimal.ZERO;
        this.returnsValue = BigDecimal.ZERO;
        this.netAmount = BigDecimal.ZERO;
        this.createdAt = Instant.EPOCH;
    }

    public @Nullable Long getId() { return id; }
    public void setId(@Nullable Long id) { this.id = id; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public @Nullable RepOrderWorkflowEntity getRepOrder() { return repOrder; }
    public void setRepOrder(@Nullable RepOrderWorkflowEntity repOrder) { this.repOrder = repOrder; }

    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }

    public BigDecimal getGrossOrderAmount() { return grossOrderAmount; }
    public void setGrossOrderAmount(BigDecimal grossOrderAmount) { this.grossOrderAmount = grossOrderAmount; }

    public BigDecimal getReturnsValue() { return returnsValue; }
    public void setReturnsValue(BigDecimal returnsValue) { this.returnsValue = returnsValue; }

    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public List<RepOrderItemWorkflowEntity> getItems() { return items; }
    public void setItems(List<RepOrderItemWorkflowEntity> items) {
        this.items.clear();
        if (items != null) {
            for (RepOrderItemWorkflowEntity item : items) {
                addItem(item);
            }
        }
    }

    public void addItem(RepOrderItemWorkflowEntity item) {
        this.items.add(item);
        item.setRepOrderShop(this);
    }

    public List<ShopReturnWorkflowEntity> getReturns() { return returns; }
    public void setReturns(List<ShopReturnWorkflowEntity> returns) {
        this.returns.clear();
        if (returns != null) {
            for (ShopReturnWorkflowEntity r : returns) {
                addReturn(r);
            }
        }
    }

    public void addReturn(ShopReturnWorkflowEntity r) {
        this.returns.add(r);
        r.setRepOrderShop(this);
    }
}
