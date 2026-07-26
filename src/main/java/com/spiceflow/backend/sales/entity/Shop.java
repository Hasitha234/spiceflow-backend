package com.spiceflow.backend.sales.entity;


import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "shops")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE shops SET deleted_at = NOW() WHERE id=?")
@SQLRestriction("deleted_at IS NULL")
public class Shop extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "outlet_id", nullable = false, length = 100)
    private String outletId;

    @Column(length = 50)
    @org.jspecify.annotations.Nullable
    private String phone;

    @Column(columnDefinition = "TEXT")
    @org.jspecify.annotations.Nullable
    private String address;

    @Column(length = 100)
    @org.jspecify.annotations.Nullable
    private String area;

    @Column(length = 100)
    @org.jspecify.annotations.Nullable
    private String route;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "assigned_rep_id")
    @org.jspecify.annotations.Nullable
    private Rep assignedRep;

    @Column(name = "outstanding_loan", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal outstandingLoan = BigDecimal.ZERO;

    @Column(precision = 10, scale = 7)
    @org.jspecify.annotations.Nullable
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    @org.jspecify.annotations.Nullable
    private BigDecimal longitude;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "qr_code_token", length = 36, unique = true)
    private String qrCodeToken;

    @PrePersist
    protected void onCreate() {
        if (this.qrCodeToken == null) {
            this.qrCodeToken = UUID.randomUUID().toString();
        }
    }
}
