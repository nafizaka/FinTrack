
package com.fintrack.fintrack.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fintrack.fintrack.dto.BudgetForm;
import com.fintrack.fintrack.model.Budget;
import com.fintrack.fintrack.model.Category;
import com.fintrack.fintrack.model.TransactionType;
import com.fintrack.fintrack.model.User;
import com.fintrack.fintrack.repository.BudgetRepository;
import com.fintrack.fintrack.repository.CategoryRepository;
import com.fintrack.fintrack.repository.TransactionRepository;
import com.fintrack.fintrack.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BudgetService budgetService;

    private User user;
    private Category category;
    private BudgetForm form;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        category = mock(Category.class);

        form = new BudgetForm();
        form.setCategoryId(10L);
        form.setAmountLimit(new BigDecimal("1000000"));
        form.setMonth(11);
        form.setYear(2026);
    }

    // 1. Mengambil daftar budget
    @Test
    void getBudgetsSuccess() {
        when(budgetRepository
                .findByUser_UserIdAndMonthAndYearOrderByCategory_CategoryNameAsc(1L, 11, 2026))
                .thenReturn(List.of(mock(Budget.class)));

        List<Budget> result = budgetService.getBudgets(1L, 11, 2026);

        assertEquals(1, result.size());
    }

    // 2. Membuat budget berhasil
    @Test
    void createBudgetSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByCategoryIdAndUser_UserId(10L, 1L))
                .thenReturn(Optional.of(category));
        when(category.getCategoryId()).thenReturn(10L);
        when(category.getType()).thenReturn(TransactionType.EXPENSE);

        when(budgetRepository
                .existsByUser_UserIdAndCategory_CategoryIdAndMonthAndYear(1L, 10L, 11, 2026))
                .thenReturn(false);

        budgetService.createBudget(form, 1L);

        verify(budgetRepository).save(any(Budget.class));
    }

    // 3. Pengguna tidak ditemukan
    @Test
    void createBudgetUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> budgetService.createBudget(form, 1L));

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    // 4. Kategori tidak ditemukan
    @Test
    void createBudgetCategoryNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByCategoryIdAndUser_UserId(10L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> budgetService.createBudget(form, 1L));

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    // 5. Kategori pemasukan tidak diperbolehkan
    @Test
    void createBudgetIncomeCategory() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByCategoryIdAndUser_UserId(10L, 1L))
                .thenReturn(Optional.of(category));
        when(category.getType()).thenReturn(TransactionType.INCOME);

        assertThrows(IllegalArgumentException.class,
                () -> budgetService.createBudget(form, 1L));

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    // 6. Budget duplikat
    @Test
    void createBudgetDuplicate() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByCategoryIdAndUser_UserId(10L, 1L))
                .thenReturn(Optional.of(category));
        when(category.getCategoryId()).thenReturn(10L);
        when(category.getType()).thenReturn(TransactionType.EXPENSE);

        when(budgetRepository
                .existsByUser_UserIdAndCategory_CategoryIdAndMonthAndYear(1L, 10L, 11, 2026))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> budgetService.createBudget(form, 1L));

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    // 7. Mengambil budget berdasarkan ID
    @Test
    void getBudgetSuccess() {
        Budget budget = mock(Budget.class);

        when(budgetRepository.findByBudgetIdAndUser_UserId(1L, 1L))
                .thenReturn(Optional.of(budget));

        Budget result = budgetService.getBudget(1L, 1L);

        assertNotNull(result);
        assertSame(budget, result);
    }

    // 8. Budget tidak ditemukan
    @Test
    void getBudgetNotFound() {
        when(budgetRepository.findByBudgetIdAndUser_UserId(1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> budgetService.getBudget(1L, 1L));
    }

    // 9. Mengubah budget berhasil
    @Test
    void updateBudgetSuccess() {
        Budget budget = mock(Budget.class);

        when(budgetRepository.findByBudgetIdAndUser_UserId(1L, 1L))
                .thenReturn(Optional.of(budget));
        when(categoryRepository.findByCategoryIdAndUser_UserId(10L, 1L))
                .thenReturn(Optional.of(category));
        when(category.getCategoryId()).thenReturn(10L);
        when(category.getType()).thenReturn(TransactionType.EXPENSE);

        when(budgetRepository
                .existsByUser_UserIdAndCategory_CategoryIdAndMonthAndYearAndBudgetIdNot(
                        1L, 10L, 11, 2026, 1L))
                .thenReturn(false);

        budgetService.updateBudget(1L, form, 1L);

        verify(budget).setCategory(category);
        verify(budget).setAmountLimit(form.getAmountLimit());
        verify(budgetRepository).save(budget);
    }

    // 10. Budget duplikat saat update
    @Test
    void updateBudgetDuplicate() {
        Budget budget = mock(Budget.class);

        when(budgetRepository.findByBudgetIdAndUser_UserId(1L, 1L))
                .thenReturn(Optional.of(budget));
        when(categoryRepository.findByCategoryIdAndUser_UserId(10L, 1L))
                .thenReturn(Optional.of(category));
        when(category.getCategoryId()).thenReturn(10L);
        when(category.getType()).thenReturn(TransactionType.EXPENSE);

        when(budgetRepository
                .existsByUser_UserIdAndCategory_CategoryIdAndMonthAndYearAndBudgetIdNot(
                        1L, 10L, 11, 2026, 1L))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> budgetService.updateBudget(1L, form, 1L));

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    // 11. Menghapus budget berhasil
    @Test
    void deleteBudgetSuccess() {
        Budget budget = mock(Budget.class);

        when(budgetRepository.findByBudgetIdAndUser_UserId(1L, 1L))
                .thenReturn(Optional.of(budget));

        budgetService.deleteBudget(1L, 1L);

        verify(budgetRepository).delete(budget);
    }

    // 12. Menghitung total pengeluaran
    @Test
    void getTotalExpenseSuccess() {
        Budget budget = mock(Budget.class);

        when(user.getUserId()).thenReturn(1L);
        when(category.getCategoryId()).thenReturn(10L);

        when(budget.getUser()).thenReturn(user);
        when(budget.getCategory()).thenReturn(category);
        when(budget.getMonth()).thenReturn(11);
        when(budget.getYear()).thenReturn(2026);

        when(transactionRepository.calculateExpenseByCategoryAndPeriod(1L, 10L, 11, 2026))
                .thenReturn(new BigDecimal("300000"));

        BigDecimal result = budgetService.getTotalExpense(budget);

        assertEquals(new BigDecimal("300000"), result);
    }

    // 13. Menghitung sisa budget
    @Test
    void getRemainingBudgetSuccess() {
        Budget budget = mock(Budget.class);

        when(user.getUserId()).thenReturn(1L);
        when(category.getCategoryId()).thenReturn(10L);

        when(budget.getAmountLimit()).thenReturn(new BigDecimal("1000000"));
        when(budget.getUser()).thenReturn(user);
        when(budget.getCategory()).thenReturn(category);
        when(budget.getMonth()).thenReturn(11);
        when(budget.getYear()).thenReturn(2026);

        when(transactionRepository.calculateExpenseByCategoryAndPeriod(1L, 10L, 11, 2026))
                .thenReturn(new BigDecimal("300000"));

        BigDecimal result = budgetService.getRemainingBudget(budget);

        assertEquals(new BigDecimal("700000"), result);
    }
}
