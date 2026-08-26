package com.blinkit.clone.repositories;

import com.blinkit.clone.dtos.ProductCardDto;
import com.blinkit.clone.entities.Product;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("""
        SELECT new com.blinkit.clone.dtos.ProductCardDto(
            p.id, p.slug, p.name, p.image, p.mrp, p.sellingPrice, p.unit,
            CASE WHEN i.quantity > 0 THEN true ELSE false END)
        FROM Product p
        LEFT JOIN Inventory i ON i.product = p AND i.darkStore.id = :storeId
        WHERE p.active = true
          AND (:categoryId IS NULL OR p.category.id = :categoryId)
          AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')))
          AND (:minPrice IS NULL OR p.sellingPrice >= :minPrice)
          AND (:maxPrice IS NULL OR p.sellingPrice <= :maxPrice)
        """)
    Page<ProductCardDto> search(
            @Param("storeId") Long storeId,
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable
    );
    Optional<Product> findBySlugAndActiveTrue(String slug);
}