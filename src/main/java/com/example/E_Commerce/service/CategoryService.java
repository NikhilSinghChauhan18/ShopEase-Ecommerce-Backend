package com.example.E_Commerce.service;

import com.example.E_Commerce.entity.Category;
import com.example.E_Commerce.entity.Product;
import com.example.E_Commerce.repository.CategoryRepository;
import com.example.E_Commerce.repository.ProductRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    // Constructor Injection
    public CategoryService(
            CategoryRepository categoryRepository,
            ProductRepository productRepository) {

        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    // 1. Get All Products By Category ID
    public List<Product> getProductsByCategoryId(Long categoryId) {

        getCategoryById(categoryId);

        return productRepository.findByCategoryId(categoryId);
    }

    // 2. Create Category
    public Category createCategory(Category category) {

        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException(
                    "Category already exists: " + category.getName()
            );
        }

        return categoryRepository.save(category);
    }

    // 3. Get All Categories
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // 4. Get Category By ID
    public Category getCategoryById(Long id) {

        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Category not found with id: " + id
                ));
    }

    // 5. Update Category
    public Category updateCategory(Long id, Category updatedCategory) {

        Category existingCategory = getCategoryById(id);

        categoryRepository.findByName(updatedCategory.getName())
                .ifPresent(category -> {
                    if (!category.getId().equals(id)) {
                        throw new IllegalArgumentException(
                                "Category name already exists: "
                                        + updatedCategory.getName()
                        );
                    }
                });

        existingCategory.setName(updatedCategory.getName());
        existingCategory.setDescription(updatedCategory.getDescription());

        return categoryRepository.save(existingCategory);
    }

    // 6. Delete Category
    public void deleteCategory(Long id) {

        Category category = getCategoryById(id);

        categoryRepository.delete(category);
    }
}