package com.example.expenses.dto;

import com.example.expenses.model.Category;
import com.example.expenses.model.Currency;
import com.example.expenses.model.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseRequestDto {

    @NotNull(message = "{validation.date.required}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    @NotBlank(message = "{validation.description.required}")
    @Size(max = 255, message = "{validation.description.size}")
    private String description;

    @NotNull(message = "{validation.amount.required}")
    @DecimalMin(value = "0.01", message = "{validation.amount.min}")
    private BigDecimal amount;

    @NotNull(message = "{validation.category.required}")
    private Category category;

    @NotNull(message = "{validation.paymentMethod.required}")
    private PaymentMethod paymentMethod;

    @NotNull(message = "{validation.currency.required}")
    private Currency currency = Currency.EUR;

    public ExpenseRequestDto() {
        this.date = LocalDate.now();
        this.currency = Currency.EUR;
    }

    public ExpenseRequestDto(
            LocalDate date,
            String description,
            BigDecimal amount,
            Category category,
            PaymentMethod paymentMethod) {
        this(date, description, amount, category, paymentMethod, Currency.EUR);
    }

    public ExpenseRequestDto(
            LocalDate date,
            String description,
            BigDecimal amount,
            Category category,
            PaymentMethod paymentMethod,
            Currency currency) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.paymentMethod = paymentMethod;
        this.currency = currency != null ? currency : Currency.EUR;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency != null ? currency : Currency.EUR;
    }
}
