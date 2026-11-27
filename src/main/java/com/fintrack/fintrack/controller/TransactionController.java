package com.fintrack.fintrack.controller;

import com.fintrack.fintrack.dto.TransactionForm;
import com.fintrack.fintrack.model.Transaction;
import com.fintrack.fintrack.model.User;
import com.fintrack.fintrack.repository.UserRepository;
import com.fintrack.fintrack.service.TransactionService;
import com.fintrack.fintrack.repository.CategoryRepository;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.fintrack.fintrack.model.TransactionType;
import java.util.List;
import java.time.LocalDate;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public TransactionController(
            TransactionService transactionService,
            UserRepository userRepository,
            CategoryRepository categoryRepository) {

        this.transactionService = transactionService;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    private User getLoggedInUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException(
                        "Pengguna tidak ditemukan"
                ));
    }

    private void addCategoriesToModel(Model model, Long userId) {
        model.addAttribute(
                "categories",
                categoryRepository.findByUser_UserIdOrderByCategoryNameAsc(userId)
        );
    }

    // Menampilkan transaksi dengan filter
    @GetMapping
    public String transactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE
            ) LocalDate startDate,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE
            ) LocalDate endDate,
            Model model) {

        User user = getLoggedInUser(userDetails);

        model.addAttribute("user", user);

        // Menampilkan kategori milik pengguna
        addCategoriesToModel(model, user.getUserId());

        // Menyimpan nilai filter agar tetap tampil setelah pencarian
        model.addAttribute("filterType", type);
        model.addAttribute("filterCategoryId", categoryId);
        model.addAttribute("filterStartDate", startDate);
        model.addAttribute("filterEndDate", endDate);

        // Memeriksa rentang tanggal
        if (startDate != null && endDate != null
                && startDate.isAfter(endDate)) {

            model.addAttribute(
                    "errorMessage",
                    "Tanggal mulai tidak boleh melebihi tanggal akhir."
            );

            model.addAttribute("transactions", List.of());

            return "transactions";
        }

        model.addAttribute(
                "transactions",
                transactionService.getFilteredTransactions(
                        user.getUserId(),
                        type,
                        categoryId,
                        startDate,
                        endDate
                )
        );

        return "transactions";
    }

    // Menampilkan form transaksi baru
    @GetMapping("/new")
    public String newTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User user = getLoggedInUser(userDetails);

        TransactionForm form = new TransactionForm();
        form.setTransactionDate(LocalDate.now());

        model.addAttribute("form", form);
        addCategoriesToModel(model, user.getUserId());

        model.addAttribute("pageTitle", "Tambah Transaksi");
        model.addAttribute("formAction", "/transactions/save");

        return "transaction-form";
    }

    // Menyimpan transaksi baru
    @PostMapping("/save")
    public String saveTransaction(
            @Valid @ModelAttribute("form") TransactionForm form,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser(userDetails);

        if (result.hasErrors()) {
            addCategoriesToModel(model, user.getUserId());
            model.addAttribute("pageTitle", "Tambah Transaksi");
            model.addAttribute("formAction", "/transactions/save");

            return "transaction-form";
        }

        transactionService.createTransaction(form, user.getUserId());

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Transaksi berhasil ditambahkan."
        );

        return "redirect:/transactions";
    }

    // Menampilkan form edit transaksi
    @GetMapping("/edit/{id}")
    public String editTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User user = getLoggedInUser(userDetails);

        Transaction transaction = transactionService.getTransaction(
                id,
                user.getUserId()
        );

        TransactionForm form = new TransactionForm();
        form.setType(transaction.getType());
        form.setAmount(transaction.getAmount());
        form.setDescription(transaction.getDescription());
        form.setTransactionDate(transaction.getTransactionDate());
        form.setCategoryId(transaction.getCategory().getCategoryId());

        model.addAttribute("form", form);
        model.addAttribute("transactionId", id);

        addCategoriesToModel(model, user.getUserId());

        model.addAttribute("pageTitle", "Edit Transaksi");
        model.addAttribute("formAction", "/transactions/update/" + id);

        return "transaction-form";
    }

    // Memperbarui transaksi
    @PostMapping("/update/{id}")
    public String updateTransaction(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") TransactionForm form,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser(userDetails);

        if (result.hasErrors()) {
            model.addAttribute("transactionId", id);
            addCategoriesToModel(model, user.getUserId());
            model.addAttribute("pageTitle", "Edit Transaksi");
            model.addAttribute("formAction", "/transactions/update/" + id);

            return "transaction-form";
        }

        transactionService.updateTransaction(
                id,
                form,
                user.getUserId()
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Transaksi berhasil diperbarui."
        );

        return "redirect:/transactions";
    }

    // Menghapus transaksi
    @PostMapping("/delete/{id}")
    public String deleteTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser(userDetails);

        transactionService.deleteTransaction(id, user.getUserId());

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Transaksi berhasil dihapus."
        );

        return "redirect:/transactions";
    }
}
