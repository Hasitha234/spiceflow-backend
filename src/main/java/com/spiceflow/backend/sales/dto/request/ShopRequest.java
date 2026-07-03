package com.spiceflow.backend.sales.dto.request;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Builder
public record ShopRequest(
    @NotBlank(message = "Name is required")
    String name,
    String ownerName,
    String phone,
    String address,
    String area,
    String route,
    Long assignedRepId,
    BigDecimal outstandingLoan,
    BigDecimal latitude,
    BigDecimal longitude,
    Boolean isActive
) {}