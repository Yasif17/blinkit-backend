package com.blinkit.clone.dtos.response;

import java.time.LocalDateTime;

public record AdminOrderDto(
        Long orderId,
        String customerName,
        String customerEmail,
        String status,
        Double totalAmount,
        String deliveryAddress,
        LocalDateTime placedAt
) {}