package com.spiceflow.backend.sales.entity;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
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
@Table(name = "loading_sheets")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE loading_sheets SET deleted_at = NOW() WHERE id=?")
@SQLRestriction("deleted_at IS NULL")
public class LoadingSheet extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rep_order_id", nullable = false)
    private RepOrder repOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(name = "loading_date", nullable = false)
    private LocalDate loadingDate;

    @Column(name = "sheet_number", length = 50)
    private String sheetNumber;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "DRAFT";

    @OneToMany(mappedBy = "loadingSheet", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @OrderBy("id ASC")
    @Builder.Default
    private List<LoadingSheetItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "loadingSheet", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @OrderBy("id ASC")
    @Builder.Default
    private List<LoadingSheetReturn> returns = new ArrayList<>();
}
