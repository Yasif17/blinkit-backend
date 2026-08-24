package com.blinkit.clone.dtos;

import java.time.LocalDateTime;

public record OrderSummaryDto(Long orderId, String status, Double totalAmount, Integer itemCount, LocalDateTime placedAt) {}