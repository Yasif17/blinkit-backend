package com.blinkit.clone.services;

import com.blinkit.clone.dtos.request.InventoryRequest;
import com.blinkit.clone.entities.DarkStore;
import com.blinkit.clone.entities.Inventory;
import com.blinkit.clone.entities.Product;
import com.blinkit.clone.exceptions.DarkStoreNotFoundException;
import com.blinkit.clone.exceptions.ProductNotFoundException;
import com.blinkit.clone.repositories.DarkStoreRepository;
import com.blinkit.clone.repositories.InventoryRepository;
import com.blinkit.clone.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private DarkStoreRepository darkStoreRepository;

    // Handles both "first time stocking this product" and "restocking" in one call
    public Inventory setStock(InventoryRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        DarkStore store = darkStoreRepository.findById(request.getDarkStoreId())
                .orElseThrow(() -> new DarkStoreNotFoundException("Dark store not found"));

        Inventory inventory = inventoryRepository
                .findByProductIdAndDarkStoreId(request.getProductId(), request.getDarkStoreId())
                .orElseGet(Inventory::new);   // create new row if none exists yet

        inventory.setProduct(product);
        inventory.setDarkStore(store);
        inventory.setQuantity(request.getQuantity());
        return inventoryRepository.save(inventory);
    }

    public List<Inventory> getByStore(Long storeId) {
        return inventoryRepository.findByDarkStoreId(storeId);
    }
}
