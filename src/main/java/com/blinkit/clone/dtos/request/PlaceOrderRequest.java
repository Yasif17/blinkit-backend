package com.blinkit.clone.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record PlaceOrderRequest(
        @NotBlank(message = "Delivery address is required") String deliveryAddress
) {}