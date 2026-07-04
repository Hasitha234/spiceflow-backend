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
@Table(name = "rep_orders")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE rep_orders SET deleted_at = NOW() WHERE id=?")
@SQLRestriction("deleted_at IS NULL")
public class RepOrder extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rep_id", nullable = false)
    private Rep rep;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "route_area", length = 100)
    private String routeArea;

    @Column(name = "total_gross_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalGrossAmount = BigDecimal.ZERO;

    @Column(name = "total_returns_value", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalReturnsValue = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal netAmount = BigDecimal.ZERO;

    @Column(name = "loading_status", nullable = false, length = 50)
    @Builder.Default
    private String loadingStatus = "DRAFT";

    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private String status = "DRAFT";

    @Column(name = "order_number", length = 50)
    private String orderNumber;

    @OneToMany(mappedBy = "repOrder", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private List<RepOrderShop> shops = new ArrayList<>();
}
