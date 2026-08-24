package com.blinkit.clone.services;

import com.blinkit.clone.dtos.ProductCardDto;
import com.blinkit.clone.dtos.ProductDetailDto;
import com.blinkit.clone.dtos.request.ProductRequest;
import com.blinkit.clone.entities.Category;
import com.blinkit.clone.entities.Inventory;
import com.blinkit.clone.entities.Product;
import com.blinkit.clone.exceptions.CategoryNotFoundException;
import com.blinkit.clone.exceptions.ProductNotFoundException;
import com.blinkit.clone.repositories.CategoryRepository;
import com.blinkit.clone.repositories.InventoryRepository;
import com.blinkit.clone.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    public Page<ProductCardDto> search(Long storeId, Long categoryId, String keyword,
                                       Double minPrice, Double maxPrice, Pageable pageable) {
        return productRepository.search(storeId, categoryId, keyword, minPrice, maxPrice, pageable);
    }

    public ProductDetailDto getDetail(String slug, Long storeId) {
        Product product = productRepository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        // Availability is store-specific — same product, different stock per store
        int availableQty = inventoryRepository
                .findByProductIdAndDarkStoreId(product.getId(), storeId)
                .map(Inventory::getQuantity)
                .orElse(0);

        return new ProductDetailDto(
                product.getId(), product.getSlug(), product.getName(), product.getImage(),
                product.getDescription(), product.getMrp(), product.getSellingPrice(),
                product.getUnit(), product.getCategory().getName(), availableQty
        );
    }

    public Product create(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        Product product = new Product();
        product.setCategory(category);
        product.setName(request.getName());
        product.setSlug(toSlug(request.getName()));
        product.setDescription(request.getDescription());
        product.setImage(request.getImage());
        product.setMrp(request.getMrp());
        product.setSellingPrice(request.getSellingPrice());
        product.setUnit(request.getUnit());
        product.setActive(true);
        return productRepository.save(product);
    }

    public Product update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        product.setCategory(category);
        product.setName(request.getName());
        product.setSlug(toSlug(request.getName()));
        product.setDescription(request.getDescription());
        product.setImage(request.getImage());
        product.setMrp(request.getMrp());
        product.setSellingPrice(request.getSellingPrice());
        product.setUnit(request.getUnit());
        return productRepository.save(product);
    }

    public void deactivate(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        product.setActive(false);
        productRepository.save(product);
    }

    private String toSlug(String name) {
        return name.trim().toLowerCase().replaceAll("[^a-z0-9\\s-]", "").replaceAll("\\s+", "-");
    }
}