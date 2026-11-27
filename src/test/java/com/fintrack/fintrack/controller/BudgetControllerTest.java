
package com.fintrack.fintrack.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.fintrack.fintrack.dto.BudgetForm;
import com.fintrack.fintrack.model.Budget;
import com.fintrack.fintrack.model.Category;
import com.fintrack.fintrack.model.User;
import com.fintrack.fintrack.repository.CategoryRepository;
import com.fintrack.fintrack.repository.UserRepository;
import com.fintrack.fintrack.service.BudgetService;

@ExtendWith(MockitoExtension.class)
class BudgetControllerTest {

    @Mock
    private BudgetService budgetService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BudgetController budgetController;

    private User user;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        when(user.getUserId()).thenReturn(1L);

        UserBuilder builder = org.springframework.security.core.userdetails.User
                .withUsername("test@example.com");

        userDetails = builder
                .password("password")
                .roles("USER")
                .build();

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
    }

    @Test
    void testShowBudgets() {
        when(budgetService.getBudgets(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();

        String view = budgetController.budgets(
                userDetails, null, null, model
        );

        assertEquals("budgets", view);
        assertTrue(model.containsAttribute("budgets"));
        assertTrue(model.containsAttribute("totalExpenses"));
        assertTrue(model.containsAttribute("remainingBudgets"));
        assertTrue(model.containsAttribute("selectedMonth"));

        verify(budgetService).getBudgets(
                eq(1L), anyInt(), anyInt()
        );
    }

    @Test
    void testShowNewBudgetForm() {
        when(categoryRepository
                .findByUser_UserIdOrderByCategoryNameAsc(1L))
                .thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();

        String view = budgetController.newBudget(userDetails, model);

        assertEquals("budget-form", view);
        assertTrue(model.containsAttribute("form"));
        assertEquals("Tambah Anggaran", model.get("pageTitle"));
        assertEquals("/budgets/save", model.get("formAction"));
    }

    @Test
    void testSaveBudgetSuccess() {
        BudgetForm form = new BudgetForm();
        form.setCategoryId(1L);
        form.setAmountLimit(new BigDecimal("1000000"));
        form.setMonth(11);
        form.setYear(2026);

        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);

        RedirectAttributesModelMap redirectAttributes =
                new RedirectAttributesModelMap();

        String view = budgetController.saveBudget(
                form,
                result,
                userDetails,
                new ExtendedModelMap(),
                redirectAttributes
        );

        assertEquals("redirect:/budgets?month=11&year=2026", view);

        verify(budgetService).createBudget(form, 1L);
        assertEquals(
                "Anggaran berhasil ditambahkan.",
                redirectAttributes.getFlashAttributes().get("successMessage")
        );
    }

    @Test
    void testSaveBudgetValidationError() {
        BudgetForm form = new BudgetForm();

        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(true);

        when(categoryRepository
                .findByUser_UserIdOrderByCategoryNameAsc(1L))
                .thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();

        String view = budgetController.saveBudget(
                form,
                result,
                userDetails,
                model,
                new RedirectAttributesModelMap()
        );

        assertEquals("budget-form", view);
        assertEquals("Tambah Anggaran", model.get("pageTitle"));

        verify(budgetService, never()).createBudget(any(), anyLong());
    }

    @Test
    void testSaveBudgetBusinessError() {
        BudgetForm form = new BudgetForm();
        form.setCategoryId(1L);
        form.setAmountLimit(new BigDecimal("500000"));
        form.setMonth(11);
        form.setYear(2026);

        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);

        doThrow(new IllegalArgumentException("Anggaran sudah ada."))
                .when(budgetService).createBudget(form, 1L);

        RedirectAttributesModelMap redirectAttributes =
                new RedirectAttributesModelMap();

        String view = budgetController.saveBudget(
                form,
                result,
                userDetails,
                new ExtendedModelMap(),
                redirectAttributes
        );

        assertEquals("redirect:/budgets?month=11&year=2026", view);
        assertEquals(
                "Anggaran sudah ada.",
                redirectAttributes.getFlashAttributes().get("errorMessage")
        );
    }

    @Test
    void testEditBudget() {
        Budget budget = mock(Budget.class);
        Category category = mock(Category.class);

        when(budgetService.getBudget(1L, 1L)).thenReturn(budget);
        when(budget.getCategory()).thenReturn(category);
        when(category.getCategoryId()).thenReturn(2L);
        when(budget.getAmountLimit()).thenReturn(new BigDecimal("1000000"));
        when(budget.getMonth()).thenReturn(11);
        when(budget.getYear()).thenReturn(2026);

        when(categoryRepository
                .findByUser_UserIdOrderByCategoryNameAsc(1L))
                .thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();

        String view = budgetController.editBudget(
                1L, userDetails, model
        );

        assertEquals("budget-form", view);
        assertEquals(1L, model.get("budgetId"));
        assertEquals("Edit Anggaran", model.get("pageTitle"));

        BudgetForm form = (BudgetForm) model.get("form");

        assertEquals(2L, form.getCategoryId());
        assertEquals(new BigDecimal("1000000"), form.getAmountLimit());
        assertEquals(11, form.getMonth());
        assertEquals(2026, form.getYear());
    }

    @Test
    void testUpdateBudgetSuccess() {
        BudgetForm form = new BudgetForm();
        form.setCategoryId(1L);
        form.setAmountLimit(new BigDecimal("1500000"));
        form.setMonth(11);
        form.setYear(2026);

        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);

        RedirectAttributesModelMap redirectAttributes =
                new RedirectAttributesModelMap();

        String view = budgetController.updateBudget(
                1L,
                form,
                result,
                userDetails,
                new ExtendedModelMap(),
                redirectAttributes
        );

        assertEquals("redirect:/budgets?month=11&year=2026", view);

        verify(budgetService).updateBudget(1L, form, 1L);

        assertEquals(
                "Anggaran berhasil diperbarui.",
                redirectAttributes.getFlashAttributes().get("successMessage")
        );
    }

    @Test
    void testUpdateBudgetValidationError() {
        BudgetForm form = new BudgetForm();

        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(true);

        when(categoryRepository
                .findByUser_UserIdOrderByCategoryNameAsc(1L))
                .thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();

        String view = budgetController.updateBudget(
                1L,
                form,
                result,
                userDetails,
                model,
                new RedirectAttributesModelMap()
        );

        assertEquals("budget-form", view);
        assertEquals(1L, model.get("budgetId"));
        assertEquals("Edit Anggaran", model.get("pageTitle"));

        verify(budgetService, never())
                .updateBudget(anyLong(), any(), anyLong());
    }

    @Test
    void testDeleteBudget() {
        Budget budget = mock(Budget.class);

        when(budgetService.getBudget(1L, 1L)).thenReturn(budget);
        when(budget.getMonth()).thenReturn(11);
        when(budget.getYear()).thenReturn(2026);

        RedirectAttributesModelMap redirectAttributes =
                new RedirectAttributesModelMap();

        String view = budgetController.deleteBudget(
                1L, userDetails, redirectAttributes
        );

        assertEquals("redirect:/budgets?month=11&year=2026", view);

        verify(budgetService).deleteBudget(1L, 1L);

        assertEquals(
                "Anggaran berhasil dihapus.",
                redirectAttributes.getFlashAttributes().get("successMessage")
        );
    }
}
