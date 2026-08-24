package com.blinkit.clone.dtos;


import lombok.Builder;


@Builder
public record ProductCardDto(
        Long id, String slug, String name, String image,
        Double mrp, Double sellingPrice, String unit, boolean inStock
) {}