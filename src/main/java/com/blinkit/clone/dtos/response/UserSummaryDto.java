package com.blinkit.clone.dtos.response;

import java.time.LocalDateTime;

public record UserSummaryDto(
        Long id,
        String name,
        String email,
        String role,
        LocalDateTime createdAt
) {}