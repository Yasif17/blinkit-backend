package com.blinkit.clone.controller;

import com.blinkit.clone.dtos.request.OrderStatusUpdateRequest;
import com.blinkit.clone.dtos.response.AdminOrderDto;
import com.blinkit.clone.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<List<AdminOrderDto>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrdersForAdmin());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id, @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        orderService.updateOrderStatus(id, request.status());
        return ResponseEntity.noContent().build();
    }
}