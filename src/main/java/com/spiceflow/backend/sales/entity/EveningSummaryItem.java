package com.spiceflow.backend.sales.entity;

import com.spiceflow.backend.common.entity.BaseEntity;
import com.spiceflow.backend.inventory.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "evening_summary_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EveningSummaryItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evening_summary_id", nullable = false)
    private EveningSummary eveningSummary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "estimate_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal estimateValue;
}
