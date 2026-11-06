package com.example.E_Commerce.repository;

import com.example.E_Commerce.entity.CartItem;
import com.example.E_Commerce.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartItem, Long> {

    // Find all cart items of a user
    List<CartItem> findByUser(User user);

    // Find a particular product in user's cart
    Optional<CartItem> findByUserIdAndProductId(
            Long userId,
            Long productId
    );

    // Delete a particular user's cart item
    void deleteByUserIdAndProductId(
            Long userId,
            Long productId
    );

    // Clear user's entire cart
    void deleteByUserId(Long userId);
}