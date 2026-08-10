package com.spiceflow.backend.sales.entity;

import com.spiceflow.backend.common.entity.BaseEntity;
import com.spiceflow.backend.inventory.entity.Product;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "cancel_summary_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelSummaryItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancel_summary_id", nullable = false)
    private CancelSummary cancelSummary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "estimate_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal estimateValue;
}
