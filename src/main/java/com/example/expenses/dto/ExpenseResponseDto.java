package com.example.expenses.dto;

import com.example.expenses.model.Category;
import com.example.expenses.model.Currency;
import com.example.expenses.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseResponseDto {

    private Long id;
    private LocalDate date;
    private String description;
    private BigDecimal amount;
    private Category category;
    private String categoryDisplayName;
    private PaymentMethod paymentMethod;
    private String paymentMethodDisplayName;
    private Currency currency;
    private String currencySymbol;
    private String currencyDisplayName;

    public ExpenseResponseDto() {
    }

    public ExpenseResponseDto(
            Long id,
            LocalDate date,
            String description,
            BigDecimal amount,
            Category category,
            PaymentMethod paymentMethod) {
        this(id, date, description, amount, category, paymentMethod, Currency.EUR);
    }

    public ExpenseResponseDto(
            Long id,
            LocalDate date,
            String description,
            BigDecimal amount,
            Category category,
            PaymentMethod paymentMethod,
            Currency currency) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.categoryDisplayName = category != null ? category.getDisplayName() : "";
        this.paymentMethod = paymentMethod;
        this.paymentMethodDisplayName = paymentMethod != null ? paymentMethod.getDisplayName() : "";
        this.currency = currency != null ? currency : Currency.EUR;
        this.currencySymbol = this.currency.getSymbol();
        this.currencyDisplayName = this.currency.getDisplayName();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        this.categoryDisplayName = category != null ? category.getDisplayName() : "";
    }

    public String getCategoryDisplayName() {
        return categoryDisplayName;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
        this.paymentMethodDisplayName = paymentMethod != null ? paymentMethod.getDisplayName() : "";
    }

    public String getPaymentMethodDisplayName() {
        return paymentMethodDisplayName;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency != null ? currency : Currency.EUR;
        this.currencySymbol = this.currency.getSymbol();
        this.currencyDisplayName = this.currency.getDisplayName();
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public String getCurrencyDisplayName() {
        return currencyDisplayName;
    }

    public void setCurrencyDisplayName(String currencyDisplayName) {
        this.currencyDisplayName = currencyDisplayName;
    }
}
