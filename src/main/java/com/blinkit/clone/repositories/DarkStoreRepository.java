package com.blinkit.clone.repositories;

import com.blinkit.clone.entities.DarkStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DarkStoreRepository extends JpaRepository<DarkStore, Long> {
    List<DarkStore> findByActiveTrue();
}