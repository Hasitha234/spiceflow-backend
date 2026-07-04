package com.spiceflow.backend.sales.collection.dto;

import com.spiceflow.backend.sales.collection.domain.CashCollection;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

/**
 * Response DTO for Cash Collection workflow operations.
 */
@Builder
public record CashCollectionResponse(
        @Nullable Long id,
        String collectionNumber,
        Long tenantId,
        Long shopId,
        @Nullable Long repId,
        LocalDate collectionDate,
        BigDecimal amount,
        String paymentMethod,
        @Nullable String chequeNo,
        @Nullable String chequeBankName,
        @Nullable LocalDate chequeDate,
        @Nullable String notes,
        String status,
        @Nullable String createdBy,
        @Nullable String confirmedBy,
        @Nullable String cancelledBy,
        @Nullable Long version,
        @Nullable Instant createdAt,
        @Nullable Instant updatedAt,
        @Nullable Instant confirmedAt,
        @Nullable Instant cancelledAt
) {
    public static CashCollectionResponse from(CashCollection collection) {
        return CashCollectionResponse.builder()
                .id(collection.getId())
                .collectionNumber(collection.getCollectionNumber())
                .tenantId(collection.getTenantId())
                .shopId(collection.getShopId())
                .repId(collection.getRepId())
                .collectionDate(collection.getCollectionDate())
                .amount(collection.getAmount())
                .paymentMethod(collection.getPaymentMethod())
                .chequeNo(collection.getChequeNo())
                .chequeBankName(collection.getChequeBankName())
                .chequeDate(collection.getChequeDate())
                .notes(collection.getNotes())
                .status(collection.getState().name())
                .createdBy(collection.getCreatedBy())
                .confirmedBy(collection.getConfirmedBy())
                .cancelledBy(collection.getCancelledBy())
                .version(collection.getVersion())
                .createdAt(collection.getCreatedAt())
                .updatedAt(collection.getUpdatedAt())
                .confirmedAt(collection.getConfirmedAt())
                .cancelledAt(collection.getCancelledAt())
                .build();
    }
}
