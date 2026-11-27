package com.fintrack.fintrack.repository;

import com.fintrack.fintrack.model.Transaction;
import com.fintrack.fintrack.model.TransactionType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.user.userId = :userId
        AND t.type = :type
    """)
    BigDecimal calculateTotal(
            @Param("userId") Long userId,
            @Param("type") TransactionType type
    );

    List<Transaction> findTop5ByUser_UserIdOrderByTransactionDateDescCreatedAtDesc(
            Long userId
    );

    List<Transaction> findByUser_UserIdOrderByTransactionDateDescCreatedAtDesc(
            Long userId
    );

    Optional<Transaction> findByTransactionIdAndUser_UserId(
            Long transactionId,
            Long userId
    );

    @Query("""
        SELECT t
        FROM Transaction t
        WHERE t.user.userId = :userId
        AND (:type IS NULL OR t.type = :type)
        AND (:categoryId IS NULL OR t.category.categoryId = :categoryId)
        AND (:startDate IS NULL OR t.transactionDate >= :startDate)
        AND (:endDate IS NULL OR t.transactionDate <= :endDate)
        ORDER BY t.transactionDate DESC, t.createdAt DESC
    """)
    List<Transaction> findFilteredTransactions(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM Transaction t
    WHERE t.user.userId = :userId
    AND t.category.categoryId = :categoryId
    AND t.type = com.fintrack.fintrack.model.TransactionType.EXPENSE
    AND MONTH(t.transactionDate) = :month
    AND YEAR(t.transactionDate) = :year
""")
    BigDecimal calculateExpenseByCategoryAndPeriod(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("month") Integer month,
            @Param("year") Integer year
    );
}
