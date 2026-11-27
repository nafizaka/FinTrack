package com.fintrack.fintrack.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class BudgetForm {

    @NotNull(message = "Nominal anggaran wajib diisi.")
    @DecimalMin(
            value = "0.01",
            message = "Anggaran harus lebih dari 0."
    )
    private BigDecimal amountLimit;

    @NotNull(message = "Bulan wajib dipilih.")
    @Min(value = 1, message = "Bulan tidak valid.")
    @Max(value = 12, message = "Bulan tidak valid.")
    private Integer month;

    @NotNull(message = "Tahun wajib diisi.")
    @Min(value = 2020, message = "Tahun tidak valid.")
    @Max(value = 2100, message = "Tahun tidak valid.")
    private Integer year;

    @NotNull(message = "Kategori wajib dipilih.")
    private Long categoryId;

    public BigDecimal getAmountLimit() {
        return amountLimit;
    }

    public void setAmountLimit(BigDecimal amountLimit) {
        this.amountLimit = amountLimit;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
