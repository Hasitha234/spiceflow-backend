package com.spiceflow.backend.sales.entity;

import com.spiceflow.backend.auth.entity.Tenant;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@org.hibernate.annotations.Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Table(name = "delivery_shops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryShop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "gross_bill_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal grossBillAmount = BigDecimal.ZERO;

    @Column(name = "total_discount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalDiscount = BigDecimal.ZERO;

    @Column(name = "returns_deducted", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal returnsDeducted = BigDecimal.ZERO;

    @Column(name = "net_payable", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal netPayable = BigDecimal.ZERO;

    @Column(name = "paid_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "credit_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal creditAmount = BigDecimal.ZERO;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "location_accuracy")
    private Double locationAccuracy;

    @Column(name = "location_verified")
    @Builder.Default
    private Boolean locationVerified = false;

    @Column(name = "distance_from_shop")
    private Double distanceFromShop;

    @Column(name = "notes", length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "deliveryShop", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private List<DeliveryShopItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "deliveryShop", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private List<DeliveryShopReturn> returns = new ArrayList<>();

    @OneToMany(mappedBy = "deliveryShop", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private List<DeliveryPayment> payments = new ArrayList<>();
}
