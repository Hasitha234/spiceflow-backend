package com.spiceflow.backend.sales.delivery.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "delivery_shop_items")
public class DeliveryShopItemWorkflowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @ManyToOne
    @JoinColumn(name = "delivery_shop_id", nullable = false)
    private DeliveryShopWorkflowEntity deliveryShop;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity_delivered", nullable = false)
    private Integer quantityDelivered;

    @Column(name = "unit_type", length = 10)
    private @Nullable String unitType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal rate;

    @Column(name = "gross_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "discount_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "net_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "is_free_item", nullable = false)
    private boolean freeItem;

    public DeliveryShopItemWorkflowEntity() {
        this.tenantId = 0L;
        this.productId = 0L;
        this.quantityDelivered = 0;
        this.rate = BigDecimal.ZERO;
        this.grossAmount = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.netAmount = BigDecimal.ZERO;
        this.deliveryShop = new DeliveryShopWorkflowEntity();
    }

    public @Nullable Long getId() { return id; }
    public void setId(@Nullable Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public DeliveryShopWorkflowEntity getDeliveryShop() { return deliveryShop; }
    public void setDeliveryShop(DeliveryShopWorkflowEntity deliveryShop) { this.deliveryShop = deliveryShop; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantityDelivered() { return quantityDelivered; }
    public void setQuantityDelivered(Integer quantityDelivered) { this.quantityDelivered = quantityDelivered; }
    public @Nullable String getUnitType() { return unitType; }
    public void setUnitType(@Nullable String unitType) { this.unitType = unitType; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
    public boolean isFreeItem() { return freeItem; }
    public void setFreeItem(boolean freeItem) { this.freeItem = freeItem; }
}
