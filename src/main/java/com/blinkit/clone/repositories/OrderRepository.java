package com.blinkit.clone.repositories;

import com.blinkit.clone.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByPlacedAtDesc(Long userId);
    Optional<Order> findByIdAndUserId(Long id, Long userId);   // ownership check baked into the query itself
}