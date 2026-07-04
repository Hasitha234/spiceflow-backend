package com.spiceflow.backend.sales.order.entity;

import com.spiceflow.backend.sales.order.domain.RepOrderState;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "rep_orders")
public class RepOrderWorkflowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(name = "order_number", unique = true)
    private @Nullable String orderNumber;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "rep_id", nullable = false)
    private Long repId;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "route_area")
    private @Nullable String routeArea;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RepOrderState status;

    @Column(name = "total_gross_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalGrossAmount;

    @Column(name = "total_returns_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalReturnsValue;

    @Column(name = "net_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "loading_status", nullable = false)
    private String loadingStatus;

    @Column(name = "created_by")
    private @Nullable String createdBy;

    @Column(name = "approved_by")
    private @Nullable String approvedBy;

    @Column(name = "loaded_by")
    private @Nullable String loadedBy;

    @Column(name = "delivered_by")
    private @Nullable String deliveredBy;

    @Version
    private @Nullable Long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "approved_at")
    private @Nullable Instant approvedAt;

    @Column(name = "loaded_at")
    private @Nullable Instant loadedAt;

    @Column(name = "delivered_at")
    private @Nullable Instant deliveredAt;

    @OneToMany(
        mappedBy = "repOrder",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<RepOrderShopWorkflowEntity> shops = new ArrayList<>();

    public RepOrderWorkflowEntity() {
        this.tenantId = 0L;
        this.repId = 0L;
        this.orderDate = LocalDate.now();
        this.status = RepOrderState.DRAFT;
        this.totalGrossAmount = BigDecimal.ZERO;
        this.totalReturnsValue = BigDecimal.ZERO;
        this.netAmount = BigDecimal.ZERO;
        this.loadingStatus = "DRAFT";
        this.createdAt = Instant.EPOCH;
        this.updatedAt = Instant.EPOCH;
    }

    public @Nullable Long getId() { return id; }
    public void setId(@Nullable Long id) { this.id = id; }

    public @Nullable String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(@Nullable String orderNumber) { this.orderNumber = orderNumber; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public Long getRepId() { return repId; }
    public void setRepId(Long repId) { this.repId = repId; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public @Nullable String getRouteArea() { return routeArea; }
    public void setRouteArea(@Nullable String routeArea) { this.routeArea = routeArea; }

    public RepOrderState getStatus() { return status; }
    public void setStatus(RepOrderState status) { this.status = status; }

    public BigDecimal getTotalGrossAmount() { return totalGrossAmount; }
    public void setTotalGrossAmount(BigDecimal totalGrossAmount) { this.totalGrossAmount = totalGrossAmount; }

    public BigDecimal getTotalReturnsValue() { return totalReturnsValue; }
    public void setTotalReturnsValue(BigDecimal totalReturnsValue) { this.totalReturnsValue = totalReturnsValue; }

    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public String getLoadingStatus() { return loadingStatus; }
    public void setLoadingStatus(String loadingStatus) { this.loadingStatus = loadingStatus; }

    public @Nullable String getCreatedBy() { return createdBy; }
    public void setCreatedBy(@Nullable String createdBy) { this.createdBy = createdBy; }

    public @Nullable String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(@Nullable String approvedBy) { this.approvedBy = approvedBy; }

    public @Nullable String getLoadedBy() { return loadedBy; }
    public void setLoadedBy(@Nullable String loadedBy) { this.loadedBy = loadedBy; }

    public @Nullable String getDeliveredBy() { return deliveredBy; }
    public void setDeliveredBy(@Nullable String deliveredBy) { this.deliveredBy = deliveredBy; }

    public @Nullable Long getVersion() { return version; }
    public void setVersion(@Nullable Long version) { this.version = version; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public @Nullable Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(@Nullable Instant approvedAt) { this.approvedAt = approvedAt; }

    public @Nullable Instant getLoadedAt() { return loadedAt; }
    public void setLoadedAt(@Nullable Instant loadedAt) { this.loadedAt = loadedAt; }

    public @Nullable Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(@Nullable Instant deliveredAt) { this.deliveredAt = deliveredAt; }

    public List<RepOrderShopWorkflowEntity> getShops() { return shops; }
    public void setShops(List<RepOrderShopWorkflowEntity> shops) {
        this.shops.clear();
        if (shops != null) {
            for (RepOrderShopWorkflowEntity shop : shops) {
                addShop(shop);
            }
        }
    }

    public void addShop(RepOrderShopWorkflowEntity shop) {
        this.shops.add(shop);
        shop.setRepOrder(this);
    }
}
