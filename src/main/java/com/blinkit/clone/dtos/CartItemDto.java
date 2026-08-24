package com.blinkit.clone.dtos;

public record CartItemDto(
        Long productId, String name, String image, Integer quantity,
        Double sellingPrice, Double lineTotal
) {}