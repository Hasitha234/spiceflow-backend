package com.spiceflow.backend.sales.entity;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.common.entity.BaseEntity;
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
@Table(name = "morning_summaries", uniqueConstraints = {
        @UniqueConstraint(name = "morning_summaries_tenant_summary_number_key", columnNames = {"tenant_id", "summary_number"})
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE morning_summaries SET deleted_at = NOW() WHERE id=?")
@SQLRestriction("deleted_at IS NULL")
public class MorningSummary extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rep_id", nullable = false)
    private Rep rep;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(name = "summary_date", nullable = false)
    private LocalDate summaryDate;

    @Column(name = "summary_number", length = 50, nullable = false)
    private String summaryNumber;

    @Column(name = "final_estimate_value", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal finalEstimateValue = BigDecimal.ZERO;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "PENDING"; // PENDING, SETTLED, CANCELLED

    @OneToMany(mappedBy = "morningSummary", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MorningSummaryItem> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deducted_warehouse_id")
    private com.spiceflow.backend.inventory.entity.Warehouse deductedWarehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_warehouse_id")
    private com.spiceflow.backend.inventory.entity.Warehouse returnWarehouse;

    public void addItem(MorningSummaryItem item) {
        items.add(item);
        item.setMorningSummary(this);
    }
}
