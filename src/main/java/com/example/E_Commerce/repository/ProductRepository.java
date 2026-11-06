package com.example.E_Commerce.repository;

import com.example.E_Commerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Search Products By Name
    List<Product> findByNameContainingIgnoreCase(String name);

    // Filter Products By Price
    List<Product> findByPriceBetween(
            BigDecimal minPrice,
            BigDecimal maxPrice
    );

    // Get Products By Category ID
    List<Product> findByCategoryId(Long categoryId);
}