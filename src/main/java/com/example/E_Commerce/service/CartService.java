
package com.example.E_Commerce.service;

import com.example.E_Commerce.dto.CartRequest;
import com.example.E_Commerce.dto.CartResponse;
import com.example.E_Commerce.entity.CartItem;
import com.example.E_Commerce.entity.Product;
import com.example.E_Commerce.entity.User;
import com.example.E_Commerce.repository.CartRepository;
import com.example.E_Commerce.repository.ProductRepository;
import com.example.E_Commerce.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
            CartRepository cartRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {

        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // 1. Add product to cart
    public CartResponse addToCart(Long userId, CartRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Product not found"));

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero");
        }

        CartItem item = cartRepository
                .findByUserIdAndProductId(userId, product.getId())
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setUser(user);
                    newItem.setProduct(product);
                    newItem.setQuantity(0);
                    return newItem;
                });

        item.setQuantity(item.getQuantity() + request.getQuantity());

        CartItem savedItem = cartRepository.save(item);

        return toResponse(savedItem);
    }

    // 2. Get user's cart
    public List<CartResponse> getCart(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        return cartRepository.findByUser(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // 3. Remove product from cart
    @Transactional
    public void removeFromCart(Long userId, Long productId) {

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }

        cartRepository.deleteByUserIdAndProductId(userId, productId);
    }

    // 4. Clear entire cart
    @Transactional
    public void clearCart(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }

        cartRepository.deleteByUserId(userId);
    }

    // Convert CartItem into CartResponse
    private CartResponse toResponse(CartItem item) {

        BigDecimal subtotal = item.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getPrice(),
                item.getQuantity(),
                subtotal
        );
    }
}