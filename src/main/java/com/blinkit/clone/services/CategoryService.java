package com.blinkit.clone.services;

import com.blinkit.clone.dtos.request.CategoryRequest;
import com.blinkit.clone.entities.Category;
import com.blinkit.clone.exceptions.CategoryAlreadyExistsException;
import com.blinkit.clone.exceptions.CategoryNotFoundException;
import com.blinkit.clone.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getAllActive() {
        return categoryRepository.findByActiveTrue();
    }

    public Category create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new CategoryAlreadyExistsException("Category already exists: " + request.getName());
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setSlug(toSlug(request.getName()));
        category.setActive(true);
        return categoryRepository.save(category);
    }

    public Category update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        category.setName(request.getName());
        category.setSlug(toSlug(request.getName()));
        return categoryRepository.save(category);
    }

    public void deactivate(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
        category.setActive(false);   // soft delete — never hard-delete, products still reference it
        categoryRepository.save(category);
    }

    private String toSlug(String name) {
        return name.trim().toLowerCase().replaceAll("[^a-z0-9\\s-]", "").replaceAll("\\s+", "-");
    }
}