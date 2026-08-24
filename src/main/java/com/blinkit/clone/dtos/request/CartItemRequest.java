package com.blinkit.clone.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record CartItemRequest(
        @NotNull Long productId,
        @NotNull @Positive Integer quantity
) {}



