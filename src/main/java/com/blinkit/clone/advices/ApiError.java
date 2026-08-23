package com.blinkit.clone.advices;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record ApiError(String message, int status, LocalDateTime timestamp, Map<String, String> fieldErrors) {}

