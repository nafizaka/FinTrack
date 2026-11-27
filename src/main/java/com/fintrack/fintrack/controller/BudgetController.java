package com.fintrack.fintrack.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fintrack.fintrack.dto.BudgetForm;
import com.fintrack.fintrack.model.Budget;
import com.fintrack.fintrack.model.Category;
import com.fintrack.fintrack.model.TransactionType;
import com.fintrack.fintrack.model.User;
import com.fintrack.fintrack.repository.CategoryRepository;
import com.fintrack.fintrack.repository.UserRepository;
import com.fintrack.fintrack.service.BudgetService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public BudgetController(
            BudgetService budgetService,
            UserRepository userRepository,
            CategoryRepository categoryRepository) {

        this.budgetService = budgetService;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    private User getLoggedInUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException(
                        "Pengguna tidak ditemukan."
                ));
    }

    private void addCategoriesToModel(Model model, Long userId) {
        List<Category> categories = categoryRepository
                .findByUser_UserIdOrderByCategoryNameAsc(userId)
                .stream()
                .filter(category
                        -> category.getType() == TransactionType.EXPENSE)
                .toList();

        model.addAttribute("categories", categories);
    }

    private void prepareFormModel(
            Model model,
            Long userId,
            String pageTitle,
            String formAction) {

        addCategoriesToModel(model, userId);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("formAction", formAction);
    }

    // Menampilkan daftar anggaran berdasarkan bulan dan tahun
    @GetMapping
public String budgets(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) Integer year,
        Model model) {

    User user = getLoggedInUser(userDetails);
    LocalDate today = LocalDate.now();

    if (month == null || month < 1 || month > 12) {
        month = today.getMonthValue();
    }

    if (year == null || year < 2020 || year > 2100) {
        year = today.getYear();
    }

    List<Budget> budgets = budgetService.getBudgets(
            user.getUserId(), month, year);

    Map<Long, BigDecimal> totalExpenses = new HashMap<>();
    Map<Long, BigDecimal> remainingBudgets = new HashMap<>();
    Map<Long, BigDecimal> percentages = new HashMap<>();
    Map<Long, BigDecimal> progressPercentages = new HashMap<>();
    Map<Long, String> progressColors = new HashMap<>();
    Map<Long, Boolean> overBudget = new HashMap<>();
    Map<Long, Boolean> warningBudget = new HashMap<>();

    for (Budget budget : budgets) {
        Long budgetId = budget.getBudgetId();

        BigDecimal expense = budgetService.getTotalExpense(budget);
        BigDecimal limit = budget.getAmountLimit();

        BigDecimal remaining = limit.subtract(expense);

        BigDecimal percentage = expense
                .multiply(new BigDecimal("100"))
                .divide(limit, 2, java.math.RoundingMode.HALF_UP);

        BigDecimal progress = percentage.min(new BigDecimal("100"));

        boolean reached = percentage.compareTo(new BigDecimal("100")) >= 0;
        boolean warning = percentage.compareTo(new BigDecimal("80")) >= 0
                && !reached;

        String color;

        if (reached) {
            color = "bg-danger";
        } else if (warning) {
            color = "bg-warning";
        } else {
            color = "bg-success";
        }

        totalExpenses.put(budgetId, expense);
        remainingBudgets.put(budgetId, remaining);
        percentages.put(budgetId, percentage);
        progressPercentages.put(budgetId, progress);
        progressColors.put(budgetId, color);
        overBudget.put(budgetId, reached);
        warningBudget.put(budgetId, warning);
    }

    model.addAttribute("user", user);
    model.addAttribute("budgets", budgets);
    model.addAttribute("totalExpenses", totalExpenses);
    model.addAttribute("remainingBudgets", remainingBudgets);
    model.addAttribute("percentages", percentages);
    model.addAttribute("progressPercentages", progressPercentages);
    model.addAttribute("progressColors", progressColors);
    model.addAttribute("overBudget", overBudget);
    model.addAttribute("warningBudget", warningBudget);
    model.addAttribute("selectedMonth", month);
    model.addAttribute("selectedYear", year);

    return "budgets";
}

    // Menampilkan form tambah anggaran
    @GetMapping("/new")
    public String newBudget(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User user = getLoggedInUser(userDetails);

        LocalDate today = LocalDate.now();

        BudgetForm form = new BudgetForm();
        form.setMonth(today.getMonthValue());
        form.setYear(today.getYear());

        model.addAttribute("form", form);

        prepareFormModel(
                model,
                user.getUserId(),
                "Tambah Anggaran",
                "/budgets/save"
        );

        return "budget-form";
    }

    // Menyimpan anggaran baru
    @PostMapping("/save")
    public String saveBudget(
            @Valid @ModelAttribute("form") BudgetForm form,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser(userDetails);

        if (result.hasErrors()) {
            prepareFormModel(
                    model,
                    user.getUserId(),
                    "Tambah Anggaran",
                    "/budgets/save"
            );

            return "budget-form";
        }

        try {
            budgetService.createBudget(form, user.getUserId());

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Anggaran berhasil ditambahkan."
            );

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/budgets?month=" + form.getMonth()
                + "&year=" + form.getYear();
    }

    // Menampilkan form edit anggaran
    @GetMapping("/edit/{id}")
    public String editBudget(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User user = getLoggedInUser(userDetails);

        Budget budget = budgetService.getBudget(id, user.getUserId());

        BudgetForm form = new BudgetForm();
        form.setCategoryId(budget.getCategory().getCategoryId());
        form.setAmountLimit(budget.getAmountLimit());
        form.setMonth(budget.getMonth());
        form.setYear(budget.getYear());

        model.addAttribute("form", form);
        model.addAttribute("budgetId", id);

        prepareFormModel(
                model,
                user.getUserId(),
                "Edit Anggaran",
                "/budgets/update/" + id
        );

        return "budget-form";
    }

    // Memperbarui anggaran
    @PostMapping("/update/{id}")
    public String updateBudget(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") BudgetForm form,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser(userDetails);

        if (result.hasErrors()) {
            model.addAttribute("budgetId", id);

            prepareFormModel(
                    model,
                    user.getUserId(),
                    "Edit Anggaran",
                    "/budgets/update/" + id
            );

            return "budget-form";
        }

        try {
            budgetService.updateBudget(
                    id, form, user.getUserId()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Anggaran berhasil diperbarui."
            );

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/budgets?month=" + form.getMonth()
                + "&year=" + form.getYear();
    }

    // Menghapus anggaran
    @PostMapping("/delete/{id}")
    public String deleteBudget(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser(userDetails);

        Budget budget = budgetService.getBudget(id, user.getUserId());

        Integer month = budget.getMonth();
        Integer year = budget.getYear();

        budgetService.deleteBudget(id, user.getUserId());

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Anggaran berhasil dihapus."
        );

        return "redirect:/budgets?month=" + month
                + "&year=" + year;
    }
}
