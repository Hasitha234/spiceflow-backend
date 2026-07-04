package com.spiceflow.backend.sales.delivery.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "delivery_shops")
public class DeliveryShopWorkflowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @ManyToOne
    @JoinColumn(name = "delivery_id", nullable = false)
    private DeliveryWorkflowEntity delivery;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "gross_bill_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal grossBillAmount;

    @Column(name = "total_discount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDiscount;

    @Column(name = "returns_deducted", nullable = false, precision = 15, scale = 2)
    private BigDecimal returnsDeducted;

    @Column(name = "net_payable", nullable = false, precision = 15, scale = 2)
    private BigDecimal netPayable;

    @Column(name = "paid_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "credit_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal creditAmount;

    @OneToMany(mappedBy = "deliveryShop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryShopItemWorkflowEntity> items = new ArrayList<>();

    @OneToMany(mappedBy = "deliveryShop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryShopReturnWorkflowEntity> returns = new ArrayList<>();

    @OneToMany(mappedBy = "deliveryShop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryPaymentWorkflowEntity> payments = new ArrayList<>();

    public DeliveryShopWorkflowEntity() {
        this.tenantId = 0L;
        this.shopId = 0L;
        this.grossBillAmount = BigDecimal.ZERO;
        this.totalDiscount = BigDecimal.ZERO;
        this.returnsDeducted = BigDecimal.ZERO;
        this.netPayable = BigDecimal.ZERO;
        this.paidAmount = BigDecimal.ZERO;
        this.creditAmount = BigDecimal.ZERO;
        this.delivery = new DeliveryWorkflowEntity();
    }

    public @Nullable Long getId() { return id; }
    public void setId(@Nullable Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public DeliveryWorkflowEntity getDelivery() { return delivery; }
    public void setDelivery(DeliveryWorkflowEntity delivery) { this.delivery = delivery; }
    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }
    public BigDecimal getGrossBillAmount() { return grossBillAmount; }
    public void setGrossBillAmount(BigDecimal v) { this.grossBillAmount = v; }
    public BigDecimal getTotalDiscount() { return totalDiscount; }
    public void setTotalDiscount(BigDecimal v) { this.totalDiscount = v; }
    public BigDecimal getReturnsDeducted() { return returnsDeducted; }
    public void setReturnsDeducted(BigDecimal v) { this.returnsDeducted = v; }
    public BigDecimal getNetPayable() { return netPayable; }
    public void setNetPayable(BigDecimal v) { this.netPayable = v; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal v) { this.paidAmount = v; }
    public BigDecimal getCreditAmount() { return creditAmount; }
    public void setCreditAmount(BigDecimal v) { this.creditAmount = v; }
    public List<DeliveryShopItemWorkflowEntity> getItems() { return items; }
    public void setItems(List<DeliveryShopItemWorkflowEntity> items) { this.items = items; }
    public List<DeliveryShopReturnWorkflowEntity> getReturns() { return returns; }
    public void setReturns(List<DeliveryShopReturnWorkflowEntity> returns) { this.returns = returns; }
    public List<DeliveryPaymentWorkflowEntity> getPayments() { return payments; }
    public void setPayments(List<DeliveryPaymentWorkflowEntity> payments) { this.payments = payments; }
}
