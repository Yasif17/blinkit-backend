package com.blinkit.clone.controller;

import com.blinkit.clone.dtos.OrderSummaryDto;
import com.blinkit.clone.dtos.request.PlaceOrderRequest;
import com.blinkit.clone.dtos.response.OrderResponse;
import com.blinkit.clone.exceptions.UserNotFoundException;
import com.blinkit.clone.repositories.UserRepository;
import com.blinkit.clone.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired private OrderService orderService;
    @Autowired
    private UserRepository userRepository;

    private Long currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"))
                .getId();
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(@Valid @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(currentUserId(), request));
    }

    @GetMapping
    public ResponseEntity<List<OrderSummaryDto>> getHistory() {
        return ResponseEntity.ok(orderService.getOrderHistory(currentUserId()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getDetail(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderDetail(currentUserId(), orderId));
    }
}