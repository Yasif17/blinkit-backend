package com.blinkit.clone.dtos.response;

import com.blinkit.clone.dtos.request.OrderItemDto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long orderId, String status, List<OrderItemDto> items,
        Double subtotal, Double deliveryFee, Double totalAmount,
        String deliveryAddress, LocalDateTime placedAt
) {}
