package com.spiceflow.backend.sales.entity;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.common.entity.BaseEntity;
import com.spiceflow.backend.sales.entity.Rep;
import com.spiceflow.backend.sales.entity.Driver;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cancel_summaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CancelSummary extends BaseEntity {

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

    @Column(name = "summary_number", nullable = false, unique = true, length = 50)
    private String summaryNumber;

    @Column(name = "final_estimate_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalEstimateValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SummaryStatus status;

    @OneToMany(mappedBy = "cancelSummary", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CancelSummaryItem> items = new ArrayList<>();

    public void addItem(CancelSummaryItem item) {
        items.add(item);
        item.setCancelSummary(this);
    }

    public void removeItem(CancelSummaryItem item) {
        items.remove(item);
        item.setCancelSummary(null);
    }
}
