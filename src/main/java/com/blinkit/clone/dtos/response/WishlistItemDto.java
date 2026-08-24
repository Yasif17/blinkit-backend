package com.blinkit.clone.dtos.response;

public record WishlistItemDto(
        Long productId,
        String slug,
        String name,
        String image,
        Double mrp,
        Double sellingPrice,
        String unit
) {}