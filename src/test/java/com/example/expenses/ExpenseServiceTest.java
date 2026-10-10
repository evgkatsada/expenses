package com.example.expenses;

import com.example.expenses.dto.ExpenseRequestDto;
import com.example.expenses.dto.ExpenseResponseDto;
import com.example.expenses.dto.MonthlySummaryDto;
import com.example.expenses.model.Category;
import com.example.expenses.model.Currency;
import com.example.expenses.model.Expense;
import com.example.expenses.model.PaymentMethod;
import com.example.expenses.repository.ExpenseRepository;
import com.example.expenses.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private Expense sampleExpense;

    @BeforeEach
    void setUp() {
        sampleExpense = new Expense(
                LocalDate.of(2026, 10, 10),
                "Grocery shopping",
                new BigDecimal("45.50"),
                Category.GROCERIES,
                PaymentMethod.CREDIT_CARD,
                Currency.EUR
        );
        sampleExpense.setId(1L);
    }

    @Test
    void testCreateExpense() {
        ExpenseRequestDto requestDto = new ExpenseRequestDto(
                sampleExpense.getDate(),
                sampleExpense.getDescription(),
                sampleExpense.getAmount(),
                sampleExpense.getCategory(),
                sampleExpense.getPaymentMethod(),
                Currency.EUR
        );

        when(expenseRepository.save(any(Expense.class))).thenReturn(sampleExpense);

        ExpenseResponseDto response = expenseService.createExpense(requestDto);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Grocery shopping", response.getDescription());
        assertEquals(new BigDecimal("45.50"), response.getAmount());
        assertEquals(Category.GROCERIES, response.getCategory());
        assertEquals("Groceries", response.getCategoryDisplayName());
        assertEquals(PaymentMethod.CREDIT_CARD, response.getPaymentMethod());
        assertEquals("Credit Card", response.getPaymentMethodDisplayName());
        assertEquals(Currency.EUR, response.getCurrency());
        assertEquals("€", response.getCurrencySymbol());
    }

    @Test
    void testGetMonthlySummary() {
        LocalDate start = LocalDate.of(2026, 10, 1);
        LocalDate end = LocalDate.of(2026, 10, 31);

        Expense expense2 = new Expense(
                LocalDate.of(2026, 10, 15),
                "Restaurant dinner",
                new BigDecimal("25.00"),
                Category.FOOD_AND_DINING,
                PaymentMethod.CASH,
                Currency.EUR
        );
        expense2.setId(2L);

        when(expenseRepository.findByDateBetweenOrderByDateDescIdDesc(start, end))
                .thenReturn(List.of(sampleExpense, expense2));

        MonthlySummaryDto summary = expenseService.getMonthlySummary(2026, 10);

        assertNotNull(summary);
        assertEquals(2, summary.getCount());
        assertEquals(new BigDecimal("70.50"), summary.getTotalAmount());
        assertEquals(new BigDecimal("45.50"), summary.getCategoryBreakdown().get(Category.GROCERIES));
        assertEquals(new BigDecimal("25.00"), summary.getCategoryBreakdown().get(Category.FOOD_AND_DINING));
        assertEquals(new BigDecimal("45.50"), summary.getPaymentMethodBreakdown().get(PaymentMethod.CREDIT_CARD));
        assertEquals(new BigDecimal("25.00"), summary.getPaymentMethodBreakdown().get(PaymentMethod.CASH));
        assertEquals(new BigDecimal("70.50"), summary.getCurrencyBreakdown().get(Currency.EUR));
    }
}
