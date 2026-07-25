package com.spiceflow.backend.sales.entity;


import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.inventory.entity.Product;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@org.hibernate.annotations.Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Table(name = "shop_returns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "rep_order_shop_id")
    private RepOrderShop repOrderShop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_type", length = 10)
    private String unitType;

    @Column(name = "credit_value", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal creditValue = BigDecimal.ZERO;

    @Column(name = "return_type", nullable = false, length = 50)
    private String returnType; // EXPIRED | DAMAGED

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "PENDING";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
