package com.blinkit.clone.services;

import com.blinkit.clone.dtos.response.WishlistItemDto;
import com.blinkit.clone.dtos.response.WishlistResponse;
import com.blinkit.clone.entities.Product;
import com.blinkit.clone.entities.User;
import com.blinkit.clone.entities.WishlistItem;
import com.blinkit.clone.exceptions.ProductAlreadyInWishlistException;
import com.blinkit.clone.exceptions.ProductNotFoundException;
import com.blinkit.clone.exceptions.UserNotFoundException;
import com.blinkit.clone.exceptions.WishlistItemNotFoundException;
import com.blinkit.clone.repositories.ProductRepository;
import com.blinkit.clone.repositories.UserRepository;
import com.blinkit.clone.repositories.WishlistItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishlistService {

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public WishlistResponse addItem(Long userId, Long productId) {
        if (wishlistItemRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new ProductAlreadyInWishlistException("Product already in wishlist");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        WishlistItem item = new WishlistItem();
        item.setUser(user);
        item.setProduct(product);
        wishlistItemRepository.save(item);

        return getWishlist(userId);
    }

    @Transactional
    public void removeItem(Long userId, Long productId) {
        if (!wishlistItemRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new WishlistItemNotFoundException("Item not in wishlist");
        }
        wishlistItemRepository.deleteByUserIdAndProductId(userId, productId);
    }

    public WishlistResponse getWishlist(Long userId) {
        List<WishlistItem> items = wishlistItemRepository.findByUserId(userId);

        List<WishlistItemDto> dtos = items.stream()
                .map(item -> new WishlistItemDto(
                        item.getProduct().getId(),
                        item.getProduct().getSlug(),
                        item.getProduct().getName(),
                        item.getProduct().getImage(),
                        item.getProduct().getMrp(),
                        item.getProduct().getSellingPrice(),
                        item.getProduct().getUnit()
                ))
                .toList();

        return new WishlistResponse(dtos, dtos.size());
    }
}