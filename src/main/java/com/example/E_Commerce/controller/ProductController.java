
package com.example.E_Commerce.controller;

import com.example.E_Commerce.dto.ProductRequest;
import com.example.E_Commerce.dto.ProductResponse;
import com.example.E_Commerce.entity.Product;
import com.example.E_Commerce.service.ProductService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Convert Request DTO into Product Model
    private Product toProduct(ProductRequest request) {

        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());

        return product;
    }

    // Convert Product Model into Response DTO
    private ProductResponse toResponse(Product product) {

        ProductResponse.CategoryInfo categoryInfo = null;

        if (product.getCategory() != null) {
            categoryInfo = new ProductResponse.CategoryInfo(
                    product.getCategory().getId(),
                    product.getCategory().getName()
            );
        }

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getImageUrl(),
                categoryInfo
        );
    }

    // 1. Add Product
    @PostMapping
    public ResponseEntity<ProductResponse> addProduct(
            @Valid @RequestBody ProductRequest request) {

        Product savedProduct = productService.addProduct(
                toProduct(request),
                request.getCategoryId()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(savedProduct));
    }

    // 2. Get All Products
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {

        List<ProductResponse> products =
                productService.getAllProducts()
                        .stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }

    // 3. Get Product By ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable Long id) {

        Product product = productService.getProductById(id);

        return ResponseEntity.ok(toResponse(product));
    }

    // 4. Update Product
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {

        Product updatedProduct = productService.updateProduct(
                id,
                toProduct(request),
                request.getCategoryId()
        );

        return ResponseEntity.ok(toResponse(updatedProduct));
    }

    // 5. Delete Product
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id) {

        productService.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }

    // 6. Search Products By Name
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(
            @RequestParam String name) {

        List<ProductResponse> products =
                productService.searchProducts(name)
                        .stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }

    // 7. Filter Products By Price
    @GetMapping("/filter")
    public ResponseEntity<List<ProductResponse>> filterProducts(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {

        List<ProductResponse> products =
                productService.filterProducts(minPrice, maxPrice)
                        .stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }

    // 8. Sort Products
    @GetMapping("/sort")
    public ResponseEntity<List<ProductResponse>> sortProducts(
            @RequestParam(defaultValue = "price") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        if (!List.of("price", "name").contains(sortBy)) {
            return ResponseEntity.badRequest().build();
        }

        if (!List.of("asc", "desc").contains(direction.toLowerCase())) {
            return ResponseEntity.badRequest().build();
        }

        List<ProductResponse> products =
                productService.sortProducts(sortBy, direction)
                        .stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }

    // 9. Get Products With Pagination
    @GetMapping("/page")
    public ResponseEntity<Page<ProductResponse>> getProductsWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        if (page < 0 || size <= 0 || size > 100) {
            return ResponseEntity.badRequest().build();
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<ProductResponse> products =
                productService.getProductsWithPagination(pageable)
                        .map(this::toResponse);

        return ResponseEntity.ok(products);
    }
}
