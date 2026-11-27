package com.fintrack.fintrack.repository;

import com.fintrack.fintrack.model.Category;
import com.fintrack.fintrack.model.TransactionType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUser_UserIdOrderByCategoryNameAsc(Long userId);

    Optional<Category> findByCategoryIdAndUser_UserId(
            Long categoryId,
            Long userId
    );

    boolean existsByUser_UserIdAndCategoryNameIgnoreCaseAndType(
            Long userId,
            String categoryName,
            TransactionType type
    );

    boolean existsByUser_UserIdAndCategoryNameIgnoreCaseAndTypeAndCategoryIdNot(
            Long userId,
            String categoryName,
            TransactionType type,
            Long categoryId
    );
}
