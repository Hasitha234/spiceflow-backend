package com.spiceflow.backend.sales.entity;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.common.entity.BaseEntity;
import com.spiceflow.backend.inventory.entity.Warehouse;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evening_summaries", uniqueConstraints = {
        @UniqueConstraint(name = "evening_summaries_tenant_summary_number_key", columnNames = {"tenant_id", "summary_number"})
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE evening_summaries SET deleted_at = NOW() WHERE id=?")
@SQLRestriction("deleted_at IS NULL")
public class EveningSummary extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rep_id", nullable = false)
    private Rep rep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(name = "summary_date", nullable = false)
    private LocalDate summaryDate;

    @Column(name = "summary_number", nullable = false, length = 50)
    private String summaryNumber;

    @Column(name = "final_estimate_value", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal finalEstimateValue = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "inventory_processed", nullable = false)
    @Builder.Default
    private boolean inventoryProcessed = false;

    @OneToMany(mappedBy = "eveningSummary", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EveningSummaryItem> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deduction_warehouse_id")
    private Warehouse deductionWarehouse;

    public void addItem(EveningSummaryItem item) {
        items.add(item);
        item.setEveningSummary(this);
    }

    @SuppressWarnings("NullAway")
    public void removeItem(EveningSummaryItem item) {
        items.remove(item);
        item.setEveningSummary(null);
    }
}
