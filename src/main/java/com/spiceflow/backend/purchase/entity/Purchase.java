package com.spiceflow.backend.purchase.entity;


import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.common.entity.BaseEntity;
import com.spiceflow.backend.inventory.entity.Supplier;
import com.spiceflow.backend.inventory.entity.Warehouse;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "purchases")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE purchases SET deleted_at = NOW() WHERE id=?")
@SQLRestriction("deleted_at IS NULL")
public class Purchase extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "invoice_no", nullable = false, length = 100)
    private String invoiceNo;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "order_no", length = 100)
    private String orderNo;

    @Column(name = "lc_no", length = 100)
    private String lcNo;

    @Column(name = "total_boxes", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalBoxes = BigDecimal.ZERO;

    @Column(name = "gross_weight_kg", precision = 10, scale = 2)
    private BigDecimal grossWeightKg;

    @Column(name = "total_order_value", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalOrderValue = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "returns_deducted_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal returnsDeductedAmount = BigDecimal.ZERO;

    @Column(name = "value_of_supply", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal valueOfSupply = BigDecimal.ZERO;

    @Column(name = "vat_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal vatAmount = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal netAmount = BigDecimal.ZERO;

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    @Column(name = "cheque_no", length = 100)
    private String chequeNo;

    @Column(name = "cheque_bank_name", length = 100)
    private String chequeBankName;

    @Column(name = "cheque_amount", precision = 15, scale = 2)
    private BigDecimal chequeAmount;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "DRAFT";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "return_warehouse_id")
    @org.jspecify.annotations.Nullable
    private Warehouse returnWarehouse;

    @OneToMany(mappedBy = "purchase", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @OrderBy("id ASC")
    @Builder.Default
    private List<PurchaseLineItem> lineItems = new ArrayList<>();

    @OneToMany(mappedBy = "purchase", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @OrderBy("id ASC")
    @Builder.Default
    private List<PurchaseReturnItem> returnItems = new ArrayList<>();
}
