package com.blinkit.clone.controller;

import com.blinkit.clone.dtos.ProductCardDto;
import com.blinkit.clone.dtos.ProductDetailDto;
import com.blinkit.clone.dtos.request.ProductRequest;
import com.blinkit.clone.entities.Product;
import com.blinkit.clone.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;


@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductCardDto>> search(
            @RequestParam Long storeId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(productService.search(
                storeId, categoryId, keyword, minPrice, maxPrice, (Pageable) PageRequest.of(page, size)));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProductDetailDto> getDetail(
            @PathVariable String slug, @RequestParam Long storeId
    ) {
        return ResponseEntity.ok(productService.getDetail(slug, storeId));
    }

    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(
            @PathVariable Long id, @Valid @RequestBody ProductRequest request
    ) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        productService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}