
package com.fintrack.fintrack.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import com.fintrack.fintrack.model.Budget;
import com.fintrack.fintrack.model.Category;
import com.fintrack.fintrack.model.TransactionType;
import com.fintrack.fintrack.model.User;

@DataJpaTest
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)

class BudgetRepositoryTest {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User createUser(String email) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword("password123");

        return userRepository.save(user);
    }

    private Budget createBudget(User user, String categoryName) {
        Category category = new Category();
        category.setUser(user);
        category.setCategoryName(categoryName);
        category.setType(TransactionType.EXPENSE);

        category = categoryRepository.save(category);

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(category);
        budget.setAmountLimit(new BigDecimal("1000000"));
        budget.setMonth(11);
        budget.setYear(2026);

        return budgetRepository.save(budget);
    }

    @Test
    void testSaveBudget() {
        User user = createUser("test1@gmail.com");
        Budget budget = createBudget(user, "Makanan");

        assertNotNull(budget.getBudgetId());
        assertEquals(
    0,
    new BigDecimal("1000000.00").compareTo(budget.getAmountLimit())
);
    }

    @Test
    void testFindBudgetByIdAndUser() {
        User user = createUser("test2@gmail.com");
        Budget budget = createBudget(user, "Transportasi");

        Optional<Budget> result =
                budgetRepository.findByBudgetIdAndUser_UserId(
                        budget.getBudgetId(),
                        user.getUserId()
                );

        assertTrue(result.isPresent());
        assertEquals(budget.getBudgetId(),
                result.get().getBudgetId());
    }

    @Test
    void testFindBudgetByWrongUser() {
        User user1 = createUser("test3@gmail.com");
        User user2 = createUser("test4@gmail.com");

        Budget budget = createBudget(user1, "Belanja");

        Optional<Budget> result =
                budgetRepository.findByBudgetIdAndUser_UserId(
                        budget.getBudgetId(),
                        user2.getUserId()
                );

        assertFalse(result.isPresent());
    }

    @Test
    void testFindBudgetsByPeriod() {
        User user = createUser("test5@gmail.com");

        createBudget(user, "Makanan");
        createBudget(user, "Transportasi");

        List<Budget> result =
                budgetRepository
                        .findByUser_UserIdAndMonthAndYearOrderByCategory_CategoryNameAsc(
                                user.getUserId(),
                                11,
                                2026
                        );

        assertEquals(2, result.size());
    }

    @Test
    void testCheckDuplicateBudget() {
        User user = createUser("test6@gmail.com");

        Budget budget = createBudget(user, "Hiburan");

        boolean exists =
                budgetRepository
                        .existsByUser_UserIdAndCategory_CategoryIdAndMonthAndYear(
                                user.getUserId(),
                                budget.getCategory().getCategoryId(),
                                11,
                                2026
                        );

        assertTrue(exists);
    }

    @Test
    void testCheckBudgetDuplicateExcludingCurrentBudget() {
        User user = createUser("test7@gmail.com");

        Budget budget = createBudget(user, "Kesehatan");

        boolean exists =
                budgetRepository
                        .existsByUser_UserIdAndCategory_CategoryIdAndMonthAndYearAndBudgetIdNot(
                                user.getUserId(),
                                budget.getCategory().getCategoryId(),
                                11,
                                2026,
                                budget.getBudgetId()
                        );

        assertFalse(exists);
    }
}
