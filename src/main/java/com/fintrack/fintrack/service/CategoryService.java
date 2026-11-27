package com.fintrack.fintrack.service;

import com.fintrack.fintrack.dto.CategoryForm;
import com.fintrack.fintrack.model.Category;
import com.fintrack.fintrack.model.User;
import com.fintrack.fintrack.repository.CategoryRepository;
import com.fintrack.fintrack.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(
            CategoryRepository categoryRepository,
            UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public List<Category> getAllCategories(Long userId) {
        return categoryRepository
                .findByUser_UserIdOrderByCategoryNameAsc(userId);
    }

    public Category getCategory(Long categoryId, Long userId) {
        return categoryRepository
                .findByCategoryIdAndUser_UserId(categoryId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Kategori tidak ditemukan"
                ));
    }

    @Transactional
    public void createCategory(CategoryForm form, Long userId) {

        String name = form.getCategoryName().trim();

        boolean exists = categoryRepository
                .existsByUser_UserIdAndCategoryNameIgnoreCaseAndType(
                        userId, name, form.getType()
                );

        if (exists) {
            throw new IllegalArgumentException(
                    "Kategori dengan nama dan jenis tersebut sudah ada."
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pengguna tidak ditemukan"
                ));

        Category category = new Category();
        category.setUser(user);
        category.setCategoryName(name);
        category.setType(form.getType());

        categoryRepository.save(category);
    }

    @Transactional
    public void updateCategory(
            Long categoryId,
            CategoryForm form,
            Long userId) {

        Category category = getCategory(categoryId, userId);

        String name = form.getCategoryName().trim();

        boolean exists = categoryRepository
                .existsByUser_UserIdAndCategoryNameIgnoreCaseAndTypeAndCategoryIdNot(
                        userId,
                        name,
                        form.getType(),
                        categoryId
                );

        if (exists) {
            throw new IllegalArgumentException(
                    "Kategori dengan nama dan jenis tersebut sudah ada."
            );
        }

        category.setCategoryName(name);
        category.setType(form.getType());

        categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long categoryId, Long userId) {

        Category category = getCategory(categoryId, userId);

        if (categoryRepository.existsById(categoryId)) {
            categoryRepository.delete(category);
        }
    }
}
