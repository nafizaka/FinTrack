package com.fintrack.fintrack.dto;

import com.fintrack.fintrack.model.TransactionType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

public class TransactionForm {

    @NotNull(message = "Jenis transaksi wajib dipilih")
    private TransactionType type;

    @NotNull(message = "Jumlah transaksi wajib diisi")
    @DecimalMin(value = "0.01", message = "Jumlah harus lebih dari 0")
    private BigDecimal amount;

    @Size(max = 255, message = "Deskripsi maksimal 255 karakter")
    private String description;

    @NotNull(message = "Tanggal transaksi wajib diisi")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;

    @NotNull(message = "Kategori wajib dipilih")
    private Long categoryId;

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
