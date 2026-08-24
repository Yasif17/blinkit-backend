package com.blinkit.clone.repositories;

import com.blinkit.clone.entities.Inventory;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductIdAndDarkStoreId(Long productId, Long darkStoreId);

    List<Inventory> findByDarkStoreId(Long darkStoreId);

    @Modifying
    @Query("""
        UPDATE Inventory i SET i.quantity = i.quantity - :qty
        WHERE i.product.id = :productId AND i.darkStore.id = :storeId AND i.quantity >= :qty
        """)
    int decrementStock(@Param("productId") Long productId, @Param("storeId") Long storeId, @Param("qty") int qty);

}