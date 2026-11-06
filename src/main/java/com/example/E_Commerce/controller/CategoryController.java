
package com.example.E_Commerce.controller;

import com.example.E_Commerce.dto.CategoryResponse;
import com.example.E_Commerce.dto.ProductResponse;
import com.example.E_Commerce.entity.Category;
import com.example.E_Commerce.entity.Product;
import com.example.E_Commerce.service.CategoryService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // Create Category
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestBody Category category) {

        Category savedCategory =
                categoryService.createCategory(category);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toCategoryResponse(savedCategory));
    }

    // Get All Categories
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {

        List<CategoryResponse> categories =
                categoryService.getAllCategories()
                        .stream()
                        .map(this::toCategoryResponse)
                        .toList();

        return ResponseEntity.ok(categories);
    }

    // Get Category By ID
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PathVariable Long id) {

        Category category =
                categoryService.getCategoryById(id);

        return ResponseEntity.ok(
                toCategoryResponse(category)
        );
    }

    // Update Category
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestBody Category category) {

        Category updatedCategory =
                categoryService.updateCategory(id, category);

        return ResponseEntity.ok(
                toCategoryResponse(updatedCategory)
        );
    }

    // Delete Category
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id) {

        categoryService.deleteCategory(id);

        return ResponseEntity.noContent().build();
    }

    // Get Products By Category ID
    @GetMapping("/{categoryId}/products")
    public ResponseEntity<List<ProductResponse>> getProductsByCategoryId(
            @PathVariable Long categoryId) {

        List<ProductResponse> products =
                categoryService.getProductsByCategoryId(categoryId)
                        .stream()
                        .map(this::toProductResponse)
                        .toList();

        return ResponseEntity.ok(products);
    }

    // Convert Category Entity to DTO
    private CategoryResponse toCategoryResponse(Category category) {

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }

    // Convert Product Entity to DTO
    private ProductResponse toProductResponse(Product product) {

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
}
