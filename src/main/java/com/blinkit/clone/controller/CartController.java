package com.blinkit.clone.controller;

import com.blinkit.clone.dtos.request.CartItemRequest;
import com.blinkit.clone.dtos.response.CartResponse;
import com.blinkit.clone.exceptions.UserNotFoundException;
import com.blinkit.clone.repositories.UserRepository;
import com.blinkit.clone.services.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;
    @Autowired private UserRepository userRepository;

    private Long currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"))
                .getId();
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCart(currentUserId()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @RequestParam Long storeId, @Valid @RequestBody CartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.addItem(currentUserId(), storeId, request));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateQuantity(
            @PathVariable Long productId, @RequestParam int quantity
    ) {
        return ResponseEntity.ok(cartService.updateQuantity(currentUserId(), productId, quantity));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeItem(currentUserId(), productId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart(currentUserId());
        return ResponseEntity.noContent().build();
    }
}