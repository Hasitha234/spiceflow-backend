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
@Table(name = "daily_balances", uniqueConstraints = {
        @UniqueConstraint(name = "uq_daily_balance_date", columnNames = {"tenant_id", "balance_date"})
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE daily_balances SET deleted_at = NOW() WHERE id=?")
@SQLRestriction("deleted_at IS NULL")
public class DailyBalance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "balance_date", nullable = false)
    private LocalDate balanceDate;

    @Column(name = "morning_summary_total", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal morningSummaryTotal = BigDecimal.ZERO;

    @Column(name = "cancel_summary_total", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal cancelSummaryTotal = BigDecimal.ZERO;

    @Column(name = "evening_summary_total", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal eveningSummaryTotal = BigDecimal.ZERO;

    @Column(name = "net_dispatch_total", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal netDispatchTotal = BigDecimal.ZERO;

    @Column(name = "bills_total", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal billsTotal = BigDecimal.ZERO;

    @Column(name = "status", length = 20, nullable = false)
    private String status;
}
