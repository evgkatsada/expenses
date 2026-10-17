package com.example.expenses;

import com.example.expenses.dto.ExpenseResponseDto;
import com.example.expenses.model.Category;
import com.example.expenses.model.Currency;
import com.example.expenses.model.PaymentMethod;
import com.example.expenses.util.ExpenseSortUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseSortUtilTest {

    private ResourceBundleMessageSource messageSource;

    private ExpenseResponseDto expense1; // 2026-10-01, 10.00 EUR, GROCERIES
    private ExpenseResponseDto expense2; // 2026-10-15, 50.00 EUR, FOOD_AND_DINING
    private ExpenseResponseDto expense3; // 2026-10-10, 25.00 EUR, UTILITIES

    @BeforeEach
    void setUp() {
        messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");

        expense1 = new ExpenseResponseDto(
                1L,
                LocalDate.of(2026, 10, 1),
                "Supermarket",
                new BigDecimal("10.00"),
                Category.GROCERIES,
                PaymentMethod.DEBIT_CARD,
                Currency.EUR
        );

        expense2 = new ExpenseResponseDto(
                2L,
                LocalDate.of(2026, 10, 15),
                "Restaurant dinner",
                new BigDecimal("50.00"),
                Category.FOOD_AND_DINING,
                PaymentMethod.CREDIT_CARD,
                Currency.EUR
        );

        expense3 = new ExpenseResponseDto(
                3L,
                LocalDate.of(2026, 10, 10),
                "Electricity bill",
                new BigDecimal("25.00"),
                Category.UTILITIES,
                PaymentMethod.BANK_TRANSFER,
                Currency.EUR
        );
    }

    @Test
    void testNormalizeSortBy() {
        assertEquals("date", ExpenseSortUtil.normalizeSortBy(null));
        assertEquals("date", ExpenseSortUtil.normalizeSortBy(""));
        assertEquals("date", ExpenseSortUtil.normalizeSortBy("invalid"));
        assertEquals("date", ExpenseSortUtil.normalizeSortBy("DATE"));
        assertEquals("amount", ExpenseSortUtil.normalizeSortBy("Amount"));
        assertEquals("category", ExpenseSortUtil.normalizeSortBy("CATEGORY"));
    }

    @Test
    void testNormalizeDirection() {
        assertEquals("desc", ExpenseSortUtil.normalizeDirection("date", null));
        assertEquals("desc", ExpenseSortUtil.normalizeDirection("amount", null));
        assertEquals("asc", ExpenseSortUtil.normalizeDirection("category", null));
        assertEquals("asc", ExpenseSortUtil.normalizeDirection("date", "asc"));
        assertEquals("desc", ExpenseSortUtil.normalizeDirection("category", "desc"));
        assertEquals("desc", ExpenseSortUtil.normalizeDirection("date", "invalid"));
    }

    @Test
    void testSortByDateAscending() {
        List<ExpenseResponseDto> list = List.of(expense2, expense1, expense3);
        List<ExpenseResponseDto> sorted = ExpenseSortUtil.sort(list, "date", "asc", messageSource, Locale.ENGLISH);

        assertEquals(3, sorted.size());
        assertEquals(1L, sorted.get(0).getId()); // 2026-10-01
        assertEquals(3L, sorted.get(1).getId()); // 2026-10-10
        assertEquals(2L, sorted.get(2).getId()); // 2026-10-15
    }

    @Test
    void testSortByDateDescending() {
        List<ExpenseResponseDto> list = List.of(expense1, expense3, expense2);
        List<ExpenseResponseDto> sorted = ExpenseSortUtil.sort(list, "date", "desc", messageSource, Locale.ENGLISH);

        assertEquals(3, sorted.size());
        assertEquals(2L, sorted.get(0).getId()); // 2026-10-15
        assertEquals(3L, sorted.get(1).getId()); // 2026-10-10
        assertEquals(1L, sorted.get(2).getId()); // 2026-10-01
    }

    @Test
    void testSortByAmountAscending() {
        List<ExpenseResponseDto> list = List.of(expense2, expense1, expense3);
        List<ExpenseResponseDto> sorted = ExpenseSortUtil.sort(list, "amount", "asc", messageSource, Locale.ENGLISH);

        assertEquals(3, sorted.size());
        assertEquals(new BigDecimal("10.00"), sorted.get(0).getAmount());
        assertEquals(new BigDecimal("25.00"), sorted.get(1).getAmount());
        assertEquals(new BigDecimal("50.00"), sorted.get(2).getAmount());
    }

    @Test
    void testSortByAmountDescending() {
        List<ExpenseResponseDto> list = List.of(expense1, expense3, expense2);
        List<ExpenseResponseDto> sorted = ExpenseSortUtil.sort(list, "amount", "desc", messageSource, Locale.ENGLISH);

        assertEquals(3, sorted.size());
        assertEquals(new BigDecimal("50.00"), sorted.get(0).getAmount());
        assertEquals(new BigDecimal("25.00"), sorted.get(1).getAmount());
        assertEquals(new BigDecimal("10.00"), sorted.get(2).getAmount());
    }

    @Test
    void testSortByCategoryEnglishAscending() {
        // In English:
        // Food & Dining (F)
        // Groceries (G)
        // Utilities (U)
        List<ExpenseResponseDto> list = List.of(expense1, expense3, expense2);
        List<ExpenseResponseDto> sorted = ExpenseSortUtil.sort(list, "category", "asc", messageSource, Locale.ENGLISH);

        assertEquals(3, sorted.size());
        assertEquals(Category.FOOD_AND_DINING, sorted.get(0).getCategory());
        assertEquals(Category.GROCERIES, sorted.get(1).getCategory());
        assertEquals(Category.UTILITIES, sorted.get(2).getCategory());
    }

    @Test
    void testSortByCategoryEnglishDescending() {
        List<ExpenseResponseDto> list = List.of(expense1, expense3, expense2);
        List<ExpenseResponseDto> sorted = ExpenseSortUtil.sort(list, "category", "desc", messageSource, Locale.ENGLISH);

        assertEquals(3, sorted.size());
        assertEquals(Category.UTILITIES, sorted.get(0).getCategory());
        assertEquals(Category.GROCERIES, sorted.get(1).getCategory());
        assertEquals(Category.FOOD_AND_DINING, sorted.get(2).getCategory());
    }

    @Test
    void testSortByCategoryGreekAscending() {
        // In Greek:
        // UTILITIES -> "Λογαριασμοί & Κοινόχρηστα" (starts with Λ)
        // GROCERIES -> "Σούπερ Μάρκετ" (starts with Σ)
        // FOOD_AND_DINING -> "Φαγητό & Εστίαση" (starts with Φ)
        // Alphabetical order: Λ < Σ < Φ
        List<ExpenseResponseDto> list = List.of(expense1, expense2, expense3);
        List<ExpenseResponseDto> sorted = ExpenseSortUtil.sort(list, "category", "asc", messageSource, Locale.forLanguageTag("el"));

        assertEquals(3, sorted.size());
        assertEquals(Category.UTILITIES, sorted.get(0).getCategory());
        assertEquals(Category.GROCERIES, sorted.get(1).getCategory());
        assertEquals(Category.FOOD_AND_DINING, sorted.get(2).getCategory());
    }

    @Test
    void testEmptyAndNullLists() {
        assertTrue(ExpenseSortUtil.sort(null, "amount", "asc", messageSource, Locale.ENGLISH).isEmpty());
        assertTrue(ExpenseSortUtil.sort(Collections.emptyList(), "date", "desc", messageSource, Locale.ENGLISH).isEmpty());
    }

    @Test
    void testTieBreaker() {
        ExpenseResponseDto tied1 = new ExpenseResponseDto(10L, LocalDate.of(2026, 10, 5), "A", new BigDecimal("20.00"), Category.GROCERIES, PaymentMethod.CASH);
        ExpenseResponseDto tied2 = new ExpenseResponseDto(20L, LocalDate.of(2026, 10, 5), "B", new BigDecimal("20.00"), Category.GROCERIES, PaymentMethod.CASH);

        List<ExpenseResponseDto> sorted = ExpenseSortUtil.sort(List.of(tied1, tied2), "amount", "desc", messageSource, Locale.ENGLISH);
        assertEquals(20L, sorted.get(0).getId());
        assertEquals(10L, sorted.get(1).getId());
    }
}
