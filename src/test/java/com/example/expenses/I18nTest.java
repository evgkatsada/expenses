package com.example.expenses;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class I18nTest {

    @Test
    void testGreekAndEnglishTranslations() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);

        Locale greek = Locale.forLanguageTag("el");
        Locale english = Locale.ENGLISH;

        // App title
        assertEquals(
                "Expense Tracker",
                messageSource.getMessage("app.title", null, english));
        assertEquals(
                "Διαχείριση Εξόδων",
                messageSource.getMessage("app.title", null, greek));

        // Form texts
        assertEquals(
                "Add Expense",
                messageSource.getMessage("form.addExpense", null, english));
        assertEquals(
                "Προσθήκη Εξόδου",
                messageSource.getMessage("form.addExpense", null, greek));

        // Categories
        assertEquals(
                "Groceries",
                messageSource.getMessage("category.GROCERIES", null, english));
        assertEquals(
                "Σούπερ Μάρκετ",
                messageSource.getMessage("category.GROCERIES", null, greek));

        assertEquals(
                "Food & Dining",
                messageSource.getMessage("category.FOOD_AND_DINING", null, english));
        assertEquals(
                "Φαγητό & Εστίαση",
                messageSource.getMessage("category.FOOD_AND_DINING", null, greek));

        // Payment Methods
        assertEquals(
                "Credit Card",
                messageSource.getMessage("paymentMethod.CREDIT_CARD", null, english));
        assertEquals(
                "Πιστωτική Κάρτα",
                messageSource.getMessage("paymentMethod.CREDIT_CARD", null, greek));

        // Currencies
        assertEquals(
                "Euro (EUR)",
                messageSource.getMessage("currency.EUR", null, english));
        assertEquals(
                "Ευρώ (EUR)",
                messageSource.getMessage("currency.EUR", null, greek));

        // Summary Cards
        assertEquals(
                "Total Expenses",
                messageSource.getMessage("summary.totalExpenses", null, english));
        assertEquals(
                "ΣΥΝΟΛΙΚΑ ΕΞΟΔΑ",
                messageSource.getMessage("summary.totalExpenses", null, greek));

        // Flash messages
        assertEquals(
                "Expense recorded successfully!",
                messageSource.getMessage("message.expense.saved", null, english));
        assertEquals(
                "Το έξοδο καταχωρήθηκε με επιτυχία!",
                messageSource.getMessage("message.expense.saved", null, greek));


        assertEquals(
                "Expense deleted successfully!",
                messageSource.getMessage("message.expense.deleted", null, english));
        assertEquals(
                "Το έξοδο διαγράφηκε με επιτυχία!",
                messageSource.getMessage("message.expense.deleted", null, greek));


        // Validations
        assertEquals(
                "Date is required",
                messageSource.getMessage("validation.date.required", null, english));
        assertEquals(
                "Η ημερομηνία είναι υποχρεωτική",
                messageSource.getMessage("validation.date.required", null, greek));
    }
}
