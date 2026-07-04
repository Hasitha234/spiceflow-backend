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
@Table(name = "delivery_shop_returns")
public class DeliveryShopReturnWorkflowEntity {

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

    @Column(name = "quantity_returned", nullable = false)
    private Integer quantityReturned;

    @Column(name = "unit_type", length = 10)
    private @Nullable String unitType;

    @Column(name = "credit_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal creditValue;

    @Column(name = "return_type", nullable = false, length = 50)
    private String returnType;

    public DeliveryShopReturnWorkflowEntity() {
        this.tenantId = 0L;
        this.productId = 0L;
        this.quantityReturned = 0;
        this.creditValue = BigDecimal.ZERO;
        this.returnType = "";
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
    public Integer getQuantityReturned() { return quantityReturned; }
    public void setQuantityReturned(Integer quantityReturned) { this.quantityReturned = quantityReturned; }
    public @Nullable String getUnitType() { return unitType; }
    public void setUnitType(@Nullable String unitType) { this.unitType = unitType; }
    public BigDecimal getCreditValue() { return creditValue; }
    public void setCreditValue(BigDecimal creditValue) { this.creditValue = creditValue; }
    public String getReturnType() { return returnType; }
    public void setReturnType(String returnType) { this.returnType = returnType; }
}
