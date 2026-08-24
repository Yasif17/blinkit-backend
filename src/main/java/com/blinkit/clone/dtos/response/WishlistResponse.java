package com.blinkit.clone.dtos.response;

import java.util.List;

public record WishlistResponse(
        List<WishlistItemDto> items,
        int totalItems
) {}