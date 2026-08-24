package com.blinkit.clone.dtos;

import lombok.Builder;

@Builder
public record ProductDetailDto(
        Long id, String slug, String name, String image, String description,
        Double mrp, Double sellingPrice, String unit,
        String categoryName, Integer availableQty
) {}