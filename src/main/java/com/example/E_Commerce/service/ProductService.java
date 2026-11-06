package com.example.E_Commerce.service;

import com.example.E_Commerce.entity.Category;
import com.example.E_Commerce.entity.Product;
import com.example.E_Commerce.exception.ProductNotFoundException;
import com.example.E_Commerce.repository.CategoryRepository;
import com.example.E_Commerce.repository.ProductRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // Constructor Dependency Injection
    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository) {

        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // 1. Add Product
    public Product addProduct(Product product, Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Category not found with id: " + categoryId
                        )
                );

        product.setCategory(category);

        return productRepository.save(product);
    }

    // 2. Get All Products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // 3. Get Product By ID
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + id
                        )
                );
    }

    // 4. Update Product
    public Product updateProduct(
            Long id,
            Product updatedProduct,
            Long categoryId) {

        Product existingProduct = getProductById(id);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Category not found with id: " + categoryId
                        )
                );

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setImageUrl(updatedProduct.getImageUrl());
        existingProduct.setCategory(category);

        return productRepository.save(existingProduct);
    }

    // 5. Delete Product
    public void deleteProduct(Long id) {

        Product existingProduct = getProductById(id);

        productRepository.delete(existingProduct);
    }

    // 6. Search Products By Name
    public List<Product> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    // 7. Filter Products By Price
    public List<Product> filterProducts(
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        return productRepository.findByPriceBetween(
                minPrice,
                maxPrice
        );
    }

    // 8. Sort Products
    public List<Product> sortProducts(
            String sortBy,
            String direction) {

        Sort.Direction sortDirection =
                Sort.Direction.fromString(direction);

        Sort sort = Sort.by(sortDirection, sortBy);

        return productRepository.findAll(sort);
    }

    // 9. Get Products With Pagination
    public Page<Product> getProductsWithPagination(
            Pageable pageable) {

        return productRepository.findAll(pageable);
    }
}