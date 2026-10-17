package com.example.expenses;

import com.example.expenses.config.I18nConfig;
import com.example.expenses.controller.ExpenseViewController;
import com.example.expenses.dto.ExpenseImportResultDto;
import com.example.expenses.dto.ExpenseResponseDto;
import com.example.expenses.dto.MonthlySummaryDto;
import com.example.expenses.model.Category;
import com.example.expenses.model.Currency;
import com.example.expenses.model.PaymentMethod;
import com.example.expenses.service.ExpenseImportService;
import com.example.expenses.service.ExpenseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseViewController.class)
@Import(I18nConfig.class)
class ExpenseViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseService expenseService;

    @MockBean
    private ExpenseImportService expenseImportService;

    @Test
    void testShowExpensesInEnglishByDefault() throws Exception {
        when(expenseService.getMonthlySummary(anyInt(), anyInt()))
                .thenReturn(new MonthlySummaryDto(2026, 10, BigDecimal.ZERO, 0, Collections.emptyMap(), Collections.emptyMap()));
        when(expenseService.getExpensesByMonth(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/expenses"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Expense Tracker")))
                .andExpect(content().string(containsString("Add Expense")))
                .andExpect(content().string(containsString("Total Expenses")))
                .andExpect(content().string(containsString("No expenses recorded for this month.")));
    }

    @Test
    void testShowExpensesInGreekWithLangParam() throws Exception {
        when(expenseService.getMonthlySummary(anyInt(), anyInt()))
                .thenReturn(new MonthlySummaryDto(2026, 10, BigDecimal.ZERO, 0, Collections.emptyMap(), Collections.emptyMap()));
        when(expenseService.getExpensesByMonth(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/expenses").param("lang", "el"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Διαχείριση Εξόδων")))
                .andExpect(content().string(containsString("Προσθήκη Εξόδου")))
                .andExpect(content().string(containsString("ΣΥΝΟΛΙΚΑ ΕΞΟΔΑ")))
                .andExpect(content().string(containsString("Δεν έχουν καταγραφεί έξοδα για αυτόν τον μήνα.")));
    }

    @Test
    void testRenderGreekCategoriesAndCurrencies() throws Exception {
        ExpenseResponseDto expenseDto = new ExpenseResponseDto(
                1L,
                LocalDate.of(2026, 10, 10),
                "Ψώνια",
                new BigDecimal("25.00"),
                Category.GROCERIES,
                PaymentMethod.CREDIT_CARD,
                Currency.EUR
        );

        when(expenseService.getMonthlySummary(anyInt(), anyInt()))
                .thenReturn(new MonthlySummaryDto(2026, 10, new BigDecimal("25.00"), 1,
                        Collections.singletonMap(Category.GROCERIES, new BigDecimal("25.00")),
                        Collections.singletonMap(PaymentMethod.CREDIT_CARD, new BigDecimal("25.00")),
                        Collections.singletonMap(Currency.EUR, new BigDecimal("25.00"))));
        when(expenseService.getExpensesByMonth(anyInt(), anyInt()))
                .thenReturn(List.of(expenseDto));

        mockMvc.perform(get("/expenses").locale(Locale.forLanguageTag("el")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Σούπερ Μάρκετ")))
                .andExpect(content().string(containsString("Πιστωτική Κάρτα")))
                .andExpect(content().string(containsString("Ευρώ (EUR)")));
    }

    @Test
    void testFlashSuccessMessageInGreek() throws Exception {
        mockMvc.perform(post("/expenses/1/delete").locale(Locale.forLanguageTag("el")))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", "Το έξοδο διαγράφηκε με επιτυχία!"));
    }

    @Test
    void testValidationErrorsInGreek() throws Exception {
        when(expenseService.getMonthlySummary(anyInt(), anyInt()))
                .thenReturn(new MonthlySummaryDto(2026, 10, BigDecimal.ZERO, 0, Collections.emptyMap(), Collections.emptyMap()));
        when(expenseService.getExpensesByMonth(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(post("/expenses")
                        .locale(Locale.forLanguageTag("el"))
                        .param("description", "")
                        .param("amount", "0")
                        .param("selectedMonth", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(containsString("Η περιγραφή δεν μπορεί να είναι κενή")))
                .andExpect(content().string(containsString("Το ποσό πρέπει να είναι τουλάχιστον 0.01")));
    }

    @Test
    void testAddExpenseSuccessInGreek() throws Exception {
        mockMvc.perform(post("/expenses")
                        .locale(Locale.forLanguageTag("el"))
                        .param("date", "2026-10-11")
                        .param("description", "Καφές")
                        .param("amount", "3.50")
                        .param("category", "FOOD_AND_DINING")
                        .param("paymentMethod", "CREDIT_CARD")
                        .param("currency", "EUR")
                        .param("selectedMonth", "2026-10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses?month=2026-10"))
                .andExpect(flash().attribute("successMessage", "Το έξοδο καταχωρήθηκε με επιτυχία!"));
    }

    @Test
    void testShowExpensesWithAmountSorting() throws Exception {
        ExpenseResponseDto expense1 = new ExpenseResponseDto(
                1L, LocalDate.of(2026, 10, 1), "Small expense", new BigDecimal("10.00"),
                Category.GROCERIES, PaymentMethod.DEBIT_CARD, Currency.EUR
        );
        ExpenseResponseDto expense2 = new ExpenseResponseDto(
                2L, LocalDate.of(2026, 10, 2), "Large expense", new BigDecimal("100.00"),
                Category.SHOPPING, PaymentMethod.CREDIT_CARD, Currency.EUR
        );

        when(expenseService.getMonthlySummary(anyInt(), anyInt()))
                .thenReturn(new MonthlySummaryDto(2026, 10, new BigDecimal("110.00"), 2, Collections.emptyMap(), Collections.emptyMap()));
        when(expenseService.getExpensesByMonth(anyInt(), anyInt()))
                .thenReturn(List.of(expense2, expense1));

        mockMvc.perform(get("/expenses")
                        .param("sortBy", "amount")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("sortBy", "amount"))
                .andExpect(model().attribute("direction", "asc"))
                .andExpect(content().string(containsString("aria-sort=\"ascending\"")))
                .andExpect(content().string(containsString("Small expense")));
    }

    @Test
    void testShowExpensesWithCategorySorting() throws Exception {
        ExpenseResponseDto expense = new ExpenseResponseDto(
                1L, LocalDate.of(2026, 10, 1), "Groceries", new BigDecimal("15.00"),
                Category.GROCERIES, PaymentMethod.DEBIT_CARD, Currency.EUR
        );

        when(expenseService.getMonthlySummary(anyInt(), anyInt()))
                .thenReturn(new MonthlySummaryDto(2026, 10, new BigDecimal("15.00"), 1, Collections.emptyMap(), Collections.emptyMap()));
        when(expenseService.getExpensesByMonth(anyInt(), anyInt()))
                .thenReturn(List.of(expense));

        mockMvc.perform(get("/expenses")
                        .param("sortBy", "category")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("sortBy", "category"))
                .andExpect(model().attribute("direction", "desc"))
                .andExpect(content().string(containsString("aria-sort=\"descending\"")));
    }

    @Test
    void testAddExpensePreservesSortParams() throws Exception {
        mockMvc.perform(post("/expenses")
                        .param("date", "2026-10-15")
                        .param("description", "Dinner")
                        .param("amount", "40.00")
                        .param("category", "FOOD_AND_DINING")
                        .param("paymentMethod", "CREDIT_CARD")
                        .param("currency", "EUR")
                        .param("selectedMonth", "2026-10")
                        .param("sortBy", "amount")
                        .param("direction", "asc"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses?month=2026-10&sortBy=amount&direction=asc"));
    }

    @Test
    void testDeleteExpensePreservesSortParams() throws Exception {
        mockMvc.perform(post("/expenses/1/delete")
                        .param("selectedMonth", "2026-10")
                        .param("sortBy", "category")
                        .param("direction", "desc"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses?month=2026-10&sortBy=category&direction=desc"));
    }

    @Test
    void testImportCsvSuccessRedirectsToTargetMonth() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "expenses.csv", "text/csv",
                "1/9/2026,Netflix,,\"8,00 €\",,,\n".getBytes()
        );

        ExpenseImportResultDto result = new ExpenseImportResultDto(1, 1, 0);
        result.setAffectedMonths(Set.of("2026-09"));

        when(expenseImportService.importExpensesFromCsv(any(MockMultipartFile.class)))
                .thenReturn(result);

        mockMvc.perform(multipart("/expenses/import")
                        .file(file)
                        .param("selectedMonth", "2026-10")
                        .param("sortBy", "amount")
                        .param("direction", "desc"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses?month=2026-09&sortBy=amount&direction=desc"))
                .andExpect(flash().attribute("successMessage", containsString("Successfully imported 1 expenses!")));
    }

    @Test
    void testImportCsvEmptyFileShowsError() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/expenses/import")
                        .file(emptyFile)
                        .param("selectedMonth", "2026-10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses?month=2026-10"))
                .andExpect(flash().attribute("errorMessage", containsString("The uploaded file is empty.")));
    }
}
