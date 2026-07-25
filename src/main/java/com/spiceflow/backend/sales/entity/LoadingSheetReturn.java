package com.spiceflow.backend.sales.entity;


import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.inventory.entity.Product;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@org.hibernate.annotations.Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Table(name = "loading_sheet_returns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoadingSheetReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "loading_sheet_id", nullable = false)
    private LoadingSheet loadingSheet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity_returned", nullable = false)
    @Builder.Default
    private Integer quantityReturned = 0;

    @Column(name = "unit_type", length = 10)
    private String unitType;

    @Column(name = "return_type", nullable = false, length = 50)
    private String returnType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
