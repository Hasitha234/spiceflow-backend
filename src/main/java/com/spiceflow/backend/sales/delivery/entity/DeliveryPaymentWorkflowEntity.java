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
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "delivery_payments")
public class DeliveryPaymentWorkflowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @ManyToOne
    @JoinColumn(name = "delivery_shop_id", nullable = false)
    private DeliveryShopWorkflowEntity deliveryShop;

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "cheque_no", length = 50)
    private @Nullable String chequeNo;

    @Column(name = "cheque_bank_name", length = 100)
    private @Nullable String chequeBankName;

    @Column(name = "cheque_date")
    private @Nullable LocalDate chequeDate;

    public DeliveryPaymentWorkflowEntity() {
        this.tenantId = 0L;
        this.paymentMethod = "";
        this.amount = BigDecimal.ZERO;
        this.deliveryShop = new DeliveryShopWorkflowEntity();
    }

    public @Nullable Long getId() { return id; }
    public void setId(@Nullable Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public DeliveryShopWorkflowEntity getDeliveryShop() { return deliveryShop; }
    public void setDeliveryShop(DeliveryShopWorkflowEntity deliveryShop) { this.deliveryShop = deliveryShop; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public @Nullable String getChequeNo() { return chequeNo; }
    public void setChequeNo(@Nullable String chequeNo) { this.chequeNo = chequeNo; }
    public @Nullable String getChequeBankName() { return chequeBankName; }
    public void setChequeBankName(@Nullable String chequeBankName) { this.chequeBankName = chequeBankName; }
    public @Nullable LocalDate getChequeDate() { return chequeDate; }
    public void setChequeDate(@Nullable LocalDate chequeDate) { this.chequeDate = chequeDate; }
}
