package com.blinkit.clone.services;

import com.blinkit.clone.dtos.CartItemDto;
import com.blinkit.clone.dtos.request.CartItemRequest;
import com.blinkit.clone.dtos.response.CartResponse;
import com.blinkit.clone.entities.Cart;
import com.blinkit.clone.entities.Inventory;
import com.blinkit.clone.entities.Product;
import com.blinkit.clone.exceptions.InsufficientStockException;
import com.blinkit.clone.exceptions.ProductNotFoundException;
import com.blinkit.clone.repositories.InventoryRepository;
import com.blinkit.clone.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    @Autowired
    @Qualifier("cartRedisTemplate")
    private RedisTemplate<String, Cart> redisTemplate;
    @Autowired private ProductRepository productRepository;
    @Autowired private InventoryRepository inventoryRepository;

    private static final Duration CART_TTL = Duration.ofDays(30);

    private String cartKey(Long userId) {
        return "cart:" + userId;
    }

    private Cart loadCart(Long userId) {
        Cart cart = redisTemplate.opsForValue().get(cartKey(userId));
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
        }
        return cart;
    }

    private void saveCart(Cart cart) {
        // Every write resets the TTL — an active cart never silently expires mid-use
        redisTemplate.opsForValue().set(cartKey(cart.getUserId()), cart, CART_TTL);
    }

    public CartResponse addItem(Long userId, Long storeId, CartItemRequest request) {
        Cart cart = loadCart(userId);

        // Cart is scoped to one store — switching stores starts a fresh cart
        if (cart.getStoreId() != null && !cart.getStoreId().equals(storeId)) {
            cart = new Cart();
            cart.setUserId(userId);
        }
        cart.setStoreId(storeId);

        int currentQty = cart.getItems().getOrDefault(request.productId(), 0);
        int newQty = currentQty + request.quantity();

        validateStock(storeId, request.productId(), newQty);

        cart.getItems().put(request.productId(), newQty);
        saveCart(cart);
        return buildResponse(cart);
    }

    public CartResponse updateQuantity(Long userId, Long productId, int quantity) {
        Cart cart = loadCart(userId);

        if (!cart.getItems().containsKey(productId)) {
            throw new ProductNotFoundException("Item not in cart");
        }

        if (quantity <= 0) {
            cart.getItems().remove(productId);   // decreasing to 0 removes it — matches how every cart UI behaves
        } else {
            validateStock(cart.getStoreId(), productId, quantity);
            cart.getItems().put(productId, quantity);
        }

        saveCart(cart);
        return buildResponse(cart);
    }

    public CartResponse removeItem(Long userId, Long productId) {
        Cart cart = loadCart(userId);
        cart.getItems().remove(productId);
        saveCart(cart);
        return buildResponse(cart);
    }

    public CartResponse getCart(Long userId) {
        return buildResponse(loadCart(userId));
    }

    public void clearCart(Long userId) {
        redisTemplate.delete(cartKey(userId));
    }

    private void validateStock(Long storeId, Long productId, int requestedQty) {
        int available = inventoryRepository.findByProductIdAndDarkStoreId(productId, storeId)
                .map(Inventory::getQuantity)
                .orElse(0);

        if (requestedQty > available) {
            throw new InsufficientStockException("Only " + available + " left in stock");
        }
    }

    private CartResponse buildResponse(Cart cart) {
        List<CartItemDto> itemDtos = new ArrayList<>();
        double subtotal = 0.0;

        for (Map.Entry<Long, Integer> entry : cart.getItems().entrySet()) {
            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found"));

            double lineTotal = product.getSellingPrice() * entry.getValue();
            subtotal += lineTotal;

            itemDtos.add(new CartItemDto(
                    product.getId(), product.getName(), product.getImage(),
                    entry.getValue(), product.getSellingPrice(), lineTotal
            ));
        }

        double deliveryFee = subtotal == 0 ? 0 : (subtotal >= 199 ? 0 : 25);   // free delivery over ₹199 — common quick-commerce pattern
        double total = subtotal + deliveryFee;

        return new CartResponse(cart.getStoreId(), itemDtos, subtotal, deliveryFee, total);
    }
}