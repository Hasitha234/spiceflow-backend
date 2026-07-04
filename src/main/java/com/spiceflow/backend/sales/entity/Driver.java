package com.spiceflow.backend.sales.entity;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.common.entity.BaseEntity;
import com.spiceflow.backend.common.enums.DriverStatus;
import com.spiceflow.backend.common.enums.LicenseClass;
import com.spiceflow.backend.inventory.entity.Warehouse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

@Entity
@Table(name = "drivers")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE drivers SET deleted_at = NOW() WHERE id=?")
@SQLRestriction("deleted_at IS NULL")
public class Driver extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "employee_id", length = 50)
    @Nullable
    private String employeeId;

    @Column(length = 100)
    @Nullable
    private String email;

    @Column(length = 50)
    @Nullable
    private String phone;

    @Column(name = "employment_date")
    @Nullable
    private LocalDate employmentDate;

    @Column(name = "termination_date")
    @Nullable
    private LocalDate terminationDate;

    @Column(name = "license_number", length = 50)
    @Nullable
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "license_class", length = 50)
    @Nullable
    private LicenseClass licenseClass;

    @Column(name = "license_expiry")
    @Nullable
    private LocalDate licenseExpiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_warehouse_id")
    @Nullable
    private Warehouse defaultWarehouse;

    @Column(name = "assigned_vehicle", length = 100)
    @Nullable
    private String assignedVehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private DriverStatus status = DriverStatus.AVAILABLE;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
