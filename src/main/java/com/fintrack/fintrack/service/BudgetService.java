package com.fintrack.fintrack.service;

import com.fintrack.fintrack.dto.BudgetForm;
import com.fintrack.fintrack.model.Budget;
import com.fintrack.fintrack.model.Category;
import com.fintrack.fintrack.model.TransactionType;
import com.fintrack.fintrack.model.User;
import com.fintrack.fintrack.repository.BudgetRepository;
import com.fintrack.fintrack.repository.CategoryRepository;
import com.fintrack.fintrack.repository.TransactionRepository;
import com.fintrack.fintrack.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public BudgetService(
            BudgetRepository budgetRepository,
            CategoryRepository categoryRepository,
            TransactionRepository transactionRepository,
            UserRepository userRepository) {

        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public List<Budget> getBudgets(
            Long userId,
            Integer month,
            Integer year) {

        return budgetRepository
                .findByUser_UserIdAndMonthAndYearOrderByCategory_CategoryNameAsc(
                        userId, month, year
                );
    }

    @Transactional
    public void createBudget(BudgetForm form, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pengguna tidak ditemukan."
                ));

        Category category = getExpenseCategory(
                form.getCategoryId(), userId
        );

        boolean exists = budgetRepository
                .existsByUser_UserIdAndCategory_CategoryIdAndMonthAndYear(
                        userId,
                        category.getCategoryId(),
                        form.getMonth(),
                        form.getYear()
                );

        if (exists) {
            throw new IllegalArgumentException(
                    "Anggaran untuk kategori dan periode tersebut sudah ada."
            );
        }

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(category);
        budget.setAmountLimit(form.getAmountLimit());
        budget.setMonth(form.getMonth());
        budget.setYear(form.getYear());

        budgetRepository.save(budget);
    }

    public Budget getBudget(Long budgetId, Long userId) {
        return budgetRepository
                .findByBudgetIdAndUser_UserId(budgetId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Anggaran tidak ditemukan."
                ));
    }

    @Transactional
    public void updateBudget(
            Long budgetId,
            BudgetForm form,
            Long userId) {

        Budget budget = getBudget(budgetId, userId);

        Category category = getExpenseCategory(
                form.getCategoryId(), userId
        );

        boolean exists = budgetRepository
                .existsByUser_UserIdAndCategory_CategoryIdAndMonthAndYearAndBudgetIdNot(
                        userId,
                        category.getCategoryId(),
                        form.getMonth(),
                        form.getYear(),
                        budgetId
                );

        if (exists) {
            throw new IllegalArgumentException(
                    "Anggaran untuk kategori dan periode tersebut sudah ada."
            );
        }

        budget.setCategory(category);
        budget.setAmountLimit(form.getAmountLimit());
        budget.setMonth(form.getMonth());
        budget.setYear(form.getYear());

        budgetRepository.save(budget);
    }

    @Transactional
    public void deleteBudget(Long budgetId, Long userId) {
        Budget budget = getBudget(budgetId, userId);
        budgetRepository.delete(budget);
    }

    public BigDecimal getTotalExpense(Budget budget) {
        return transactionRepository.calculateExpenseByCategoryAndPeriod(
                budget.getUser().getUserId(),
                budget.getCategory().getCategoryId(),
                budget.getMonth(),
                budget.getYear()
        );
    }

    public BigDecimal getRemainingBudget(Budget budget) {
        return budget.getAmountLimit().subtract(
                getTotalExpense(budget)
        );
    }

    private Category getExpenseCategory(
            Long categoryId,
            Long userId) {

        Category category = categoryRepository
                .findByCategoryIdAndUser_UserId(categoryId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Kategori tidak ditemukan atau bukan milik pengguna."
                ));

        if (category.getType() != TransactionType.EXPENSE) {
            throw new IllegalArgumentException(
                    "Anggaran hanya dapat dibuat untuk kategori pengeluaran."
            );
        }

        return category;
    }
}
