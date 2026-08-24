package com.blinkit.clone.dtos.request;

public record OrderItemDto(Long productId, String name, String image, Integer quantity, Double priceAtOrder, Double lineTotal) {}
