package com.blinkit.clone.dtos.response;

public record AuthResponse(String token,String tokenType,Long userId,String name,String role) {
}
