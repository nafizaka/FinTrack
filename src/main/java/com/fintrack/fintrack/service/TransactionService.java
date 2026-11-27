package com.fintrack.fintrack.service;

import com.fintrack.fintrack.dto.TransactionForm;
import com.fintrack.fintrack.model.Category;
import com.fintrack.fintrack.model.Transaction;
import com.fintrack.fintrack.model.User;
import com.fintrack.fintrack.repository.CategoryRepository;
import com.fintrack.fintrack.repository.TransactionRepository;
import com.fintrack.fintrack.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fintrack.fintrack.model.TransactionType;
import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository) {

        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public List<Transaction> getAllTransactions(Long userId) {
        return transactionRepository
                .findByUser_UserIdOrderByTransactionDateDescCreatedAtDesc(userId);
    }

    public Transaction getTransaction(Long transactionId, Long userId) {
        return transactionRepository
                .findByTransactionIdAndUser_UserId(transactionId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaksi tidak ditemukan"
                ));
    }

    @Transactional
    public void createTransaction(TransactionForm form, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pengguna tidak ditemukan"
                ));

        Category category = getOwnedCategory(form.getCategoryId(), userId);

        Transaction transaction = new Transaction();

        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setType(form.getType());
        transaction.setAmount(form.getAmount());
        transaction.setDescription(form.getDescription());
        transaction.setTransactionDate(form.getTransactionDate());

        transactionRepository.save(transaction);
    }

    @Transactional
    public void updateTransaction(
            Long transactionId,
            TransactionForm form,
            Long userId) {

        Transaction transaction = getTransaction(transactionId, userId);

        Category category = getOwnedCategory(form.getCategoryId(), userId);

        transaction.setCategory(category);
        transaction.setType(form.getType());
        transaction.setAmount(form.getAmount());
        transaction.setDescription(form.getDescription());
        transaction.setTransactionDate(form.getTransactionDate());

        transactionRepository.save(transaction);
    }

    @Transactional
    public void deleteTransaction(Long transactionId, Long userId) {

        Transaction transaction = getTransaction(transactionId, userId);

        transactionRepository.delete(transaction);
    }

    private Category getOwnedCategory(Long categoryId, Long userId) {
        return categoryRepository
                .findByCategoryIdAndUser_UserId(categoryId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Kategori tidak ditemukan atau bukan milik pengguna"
                ));
    }

    public List<Transaction> getFilteredTransactions(
            Long userId,
            TransactionType type,
            Long categoryId,
            LocalDate startDate,
            LocalDate endDate) {

        return transactionRepository.findFilteredTransactions(
                userId,
                type,
                categoryId,
                startDate,
                endDate
        );
    }
}
