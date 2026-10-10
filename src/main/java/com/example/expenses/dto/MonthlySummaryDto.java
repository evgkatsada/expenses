package com.example.expenses.dto;

import com.example.expenses.model.Category;
import com.example.expenses.model.PaymentMethod;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

public class MonthlySummaryDto {

    private int year;
    private int month;
    private BigDecimal totalAmount;
    private int count;
    private Map<Category, BigDecimal> categoryBreakdown;
    private Map<PaymentMethod, BigDecimal> paymentMethodBreakdown;

    public MonthlySummaryDto() {
        this.totalAmount = BigDecimal.ZERO;
        this.count = 0;
        this.categoryBreakdown = Collections.emptyMap();
        this.paymentMethodBreakdown = Collections.emptyMap();
    }

    public MonthlySummaryDto(int year, int month, BigDecimal totalAmount, int count,
                             Map<Category, BigDecimal> categoryBreakdown,
                             Map<PaymentMethod, BigDecimal> paymentMethodBreakdown) {
        this.year = year;
        this.month = month;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        this.count = count;
        this.categoryBreakdown = categoryBreakdown != null ? categoryBreakdown : Collections.emptyMap();
        this.paymentMethodBreakdown = paymentMethodBreakdown != null ? paymentMethodBreakdown : Collections.emptyMap();
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Map<Category, BigDecimal> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    public void setCategoryBreakdown(Map<Category, BigDecimal> categoryBreakdown) {
        this.categoryBreakdown = categoryBreakdown;
    }

    public Map<PaymentMethod, BigDecimal> getPaymentMethodBreakdown() {
        return paymentMethodBreakdown;
    }

    public void setPaymentMethodBreakdown(Map<PaymentMethod, BigDecimal> paymentMethodBreakdown) {
        this.paymentMethodBreakdown = paymentMethodBreakdown;
    }
}
