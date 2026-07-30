package com.spiceflow.backend.sales.entity;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "bills", uniqueConstraints = {
        @UniqueConstraint(name = "uq_bills_number", columnNames = {"tenant_id", "bill_number"}),
        @UniqueConstraint(name = "uq_bills_shop_date", columnNames = {"tenant_id", "shop_id", "bill_date"})
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE bills SET deleted_at = NOW() WHERE id=?")
@SQLRestriction("deleted_at IS NULL")
public class Bill extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rep_id", nullable = false)
    private Rep rep;

    @org.jspecify.annotations.Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "bill_number", length = 50, nullable = false)
    private String billNumber;

    @Column(name = "bill_date", nullable = false)
    private LocalDate billDate;

    @Column(name = "net_total", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal netTotal = BigDecimal.ZERO;

    @Column(name = "reverse_grts", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal reverseGrts = BigDecimal.ZERO;

    @Column(name = "free_items_value", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal freeItemsValue = BigDecimal.ZERO;

    @Column(name = "discount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "sku_discount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal skuDiscount = BigDecimal.ZERO;

    @Column(name = "final_total", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal finalTotal = BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING"; // PENDING, COLLECTED, CANCELLED

    @Column(name = "cash_collected", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal cashCollected = BigDecimal.ZERO;

    @Column(name = "check_collected", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal checkCollected = BigDecimal.ZERO;

    @Column(name = "loan_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal loanAmount = BigDecimal.ZERO;

    @org.jspecify.annotations.Nullable
    @Column(name = "loan_due_date")
    private LocalDate loanDueDate;

    @Column(name = "loan_status", nullable = false, length = 20)
    @Builder.Default
    private String loanStatus = "NONE"; // NONE, UNPAID, PAID
}
