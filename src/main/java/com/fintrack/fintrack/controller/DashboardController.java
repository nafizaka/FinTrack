package com.fintrack.fintrack.controller;

import com.fintrack.fintrack.model.Transaction;
import com.fintrack.fintrack.model.TransactionType;
import com.fintrack.fintrack.model.User;
import com.fintrack.fintrack.repository.TransactionRepository;
import com.fintrack.fintrack.repository.UserRepository;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class DashboardController {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public DashboardController(
            UserRepository userRepository,
            TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(()
                        -> new RuntimeException("Pengguna tidak ditemukan")
                );

        Long userId = user.getUserId();

        BigDecimal income = transactionRepository.calculateTotal(
                userId, TransactionType.INCOME
        );

        BigDecimal expense = transactionRepository.calculateTotal(
                userId, TransactionType.EXPENSE
        );

        BigDecimal balance = income.subtract(expense);

        List<Transaction> recentTransactions
                = transactionRepository
                        .findTop5ByUser_UserIdOrderByTransactionDateDescCreatedAtDesc(
                                userId
                        );

        model.addAttribute("user", user);
        model.addAttribute("income", income);
        model.addAttribute("expense", expense);
        model.addAttribute("balance", balance);
        model.addAttribute("recentTransactions", recentTransactions);

        return "dashboard";
    }
}
