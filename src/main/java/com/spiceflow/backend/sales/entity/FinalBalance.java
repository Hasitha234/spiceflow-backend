package com.spiceflow.backend.sales.entity;

import com.spiceflow.backend.common.entity.BaseEntity;
import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.sales.entity.Driver;
import com.spiceflow.backend.auth.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "final_balances")
@Getter
@Setter
@SuppressWarnings("NullAway.Init")
public class FinalBalance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rep_id", nullable = false)
    private User rep;

    @org.jspecify.annotations.Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Column(name = "balance_date", nullable = false)
    private LocalDate balanceDate;

    @Column(name = "morning_summary_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal morningSummaryValue = BigDecimal.ZERO;

    @Column(name = "cancel_summary_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal cancelSummaryValue = BigDecimal.ZERO;

    @Column(name = "total_bill_collections", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalBillCollections = BigDecimal.ZERO;

    @Column(name = "mismatch_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal mismatchValue = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BalanceStatus status;

    @org.jspecify.annotations.Nullable
    @Column(columnDefinition = "TEXT")
    private String remarks;

    public enum BalanceStatus {
        BALANCED,
        MISMATCHED
    }
}
