package com.blinkit.clone.services;

import com.blinkit.clone.dtos.CartItemDto;
import com.blinkit.clone.dtos.OrderSummaryDto;
import com.blinkit.clone.dtos.request.OrderItemDto;
import com.blinkit.clone.dtos.request.PlaceOrderRequest;
import com.blinkit.clone.dtos.response.AdminOrderDto;
import com.blinkit.clone.dtos.response.CartResponse;
import com.blinkit.clone.dtos.response.OrderResponse;
import com.blinkit.clone.entities.DarkStore;
import com.blinkit.clone.entities.Order;
import com.blinkit.clone.entities.OrderItem;
import com.blinkit.clone.entities.User;
import com.blinkit.clone.enums.OrderStatus;
import com.blinkit.clone.exceptions.*;
import com.blinkit.clone.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private DarkStoreRepository darkStoreRepository;
    @Autowired private CartService cartService;
    @Autowired private UserRepository userRepository;

    @Transactional
    public OrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
        CartResponse cart = cartService.getCart(userId);

        if (cart.items().isEmpty()) {
            throw new EmptyCartException("Cart is empty");
        }

        // Reserve stock for every item FIRST — if any fails, the whole transaction rolls back,
        // so we never end up with an order that's missing an item's stock
        for (CartItemDto item : cart.items()) {
            int updated = inventoryRepository.decrementStock(item.productId(), cart.storeId(), item.quantity());
            if (updated == 0) {
                throw new InsufficientStockException("Item went out of stock: " + item.name());
            }
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        DarkStore store = darkStoreRepository.findById(cart.storeId())
                .orElseThrow(() -> new DarkStoreNotFoundException("Store not found"));

        Order order = new Order();
        order.setUser(user);
        order.setStore(store);
        order.setSubtotal(cart.subtotal());
        order.setDeliveryFee(cart.deliveryFee());
        order.setTotalAmount(cart.total());
        order.setDeliveryAddress(request.deliveryAddress());
        order.setStatus(OrderStatus.PLACED);

        for (CartItemDto item : cart.items()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(productRepository.getReferenceById(item.productId()));
            orderItem.setQuantity(item.quantity());
            orderItem.setPriceAtOrder(item.sellingPrice());
            order.getItems().add(orderItem);
        }

        order = orderRepository.save(order);   // cascades and saves OrderItems too
        cartService.clearCart(userId);          // cart is only cleared AFTER order is safely persisted

        return toResponse(order);
    }

    public List<OrderSummaryDto> getOrderHistory(Long userId) {
        return orderRepository.findByUserIdOrderByPlacedAtDesc(userId).stream()
                .map(o -> new OrderSummaryDto(o.getId(), o.getStatus().name(), o.getTotalAmount(), o.getItems().size(), o.getPlacedAt()))
                .toList();
    }

    public OrderResponse getOrderDetail(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        return toResponse(order);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemDto> items = order.getItems().stream()
                .map(i -> new OrderItemDto(
                        i.getProduct().getId(), i.getProduct().getName(), i.getProduct().getImage(),
                        i.getQuantity(), i.getPriceAtOrder(), i.getPriceAtOrder() * i.getQuantity()
                )).toList();

        return new OrderResponse(
                order.getId(), order.getStatus().name(), items,
                order.getSubtotal(), order.getDeliveryFee(), order.getTotalAmount(),
                order.getDeliveryAddress(), order.getPlacedAt()
        );
    }

    public List<AdminOrderDto> getAllOrdersForAdmin() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "placedAt")).stream()
                .map(o -> new AdminOrderDto(
                        o.getId(), o.getUser().getName(), o.getUser().getEmail(),
                        o.getStatus().name(), o.getTotalAmount(), o.getDeliveryAddress(), o.getPlacedAt()
                ))
                .toList();
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        order.setStatus(status);
        orderRepository.save(order);
    }

}