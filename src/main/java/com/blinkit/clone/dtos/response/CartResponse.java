package com.blinkit.clone.dtos.response;

import com.blinkit.clone.dtos.CartItemDto;

import java.util.List;

public record CartResponse(
        Long storeId, List<CartItemDto> items,
        Double subtotal, Double deliveryFee, Double total
) {}
