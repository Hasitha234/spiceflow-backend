package com.spiceflow.backend.sales.entity;


import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.inventory.entity.Warehouse;
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
@Table(name = "rep_order_shops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepOrderShop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "rep_order_id", nullable = false)
    private RepOrder repOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "gross_order_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal grossOrderAmount = BigDecimal.ZERO;

    @Column(name = "returns_value", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal returnsValue = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "sku_discount_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal skuDiscountAmount = BigDecimal.ZERO;

    @Column(name = "reverse_grts", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal reverseGrts = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal netAmount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "return_warehouse_id")
    private Warehouse returnWarehouse;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "repOrderShop", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private List<RepOrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "repOrderShop", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private List<ShopReturn> returns = new ArrayList<>();
}
