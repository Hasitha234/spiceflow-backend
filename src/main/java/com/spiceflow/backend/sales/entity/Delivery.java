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
@Table(name = "deliveries")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE deliveries SET deleted_at = NOW() WHERE id=?")
@SQLRestriction("deleted_at IS NULL")
public class Delivery extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loading_sheet_id", nullable = false)
    private LoadingSheet loadingSheet;

    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "IN_PROGRESS";

    @Column(name = "delivery_number", length = 50)
    private String deliveryNumber;

    @Column(name = "total_sales_value", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalSalesValue = BigDecimal.ZERO;

    @Column(name = "total_returns_value", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalReturnsValue = BigDecimal.ZERO;

    @Column(name = "total_collected_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalCollectedAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "delivery", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private List<DeliveryShop> shops = new ArrayList<>();
}
