package com.fintrack.fintrack.repository;

import com.fintrack.fintrack.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUser_UserIdAndMonthAndYearOrderByCategory_CategoryNameAsc(
            Long userId,
            Integer month,
            Integer year
    );

    Optional<Budget> findByBudgetIdAndUser_UserId(
            Long budgetId,
            Long userId
    );

    boolean existsByUser_UserIdAndCategory_CategoryIdAndMonthAndYear(
            Long userId,
            Long categoryId,
            Integer month,
            Integer year
    );

    boolean existsByUser_UserIdAndCategory_CategoryIdAndMonthAndYearAndBudgetIdNot(
            Long userId,
            Long categoryId,
            Integer month,
            Integer year,
            Long budgetId
    );
}
