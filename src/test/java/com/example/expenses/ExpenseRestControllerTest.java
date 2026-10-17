package com.example.expenses;

import com.example.expenses.config.I18nConfig;
import com.example.expenses.controller.ExpenseRestController;
import com.example.expenses.dto.ExpenseImportResultDto;
import com.example.expenses.service.ExpenseImportService;
import com.example.expenses.service.ExpenseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Locale;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseRestController.class)
@Import(I18nConfig.class)
class ExpenseRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseService expenseService;

    @MockBean
    private ExpenseImportService expenseImportService;

    @Test
    void testGetMetadataInEnglishByDefault() throws Exception {
        mockMvc.perform(get("/api/v1/expenses/metadata"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.categories[?(@.key == 'GROCERIES')].displayName", contains("Groceries")))
                .andExpect(jsonPath("$.categories[?(@.key == 'GROCERIES')].localizedName", contains("Groceries")))
                .andExpect(jsonPath("$.paymentMethods[?(@.key == 'CREDIT_CARD')].localizedName", contains("Credit Card")))
                .andExpect(jsonPath("$.currencies[?(@.key == 'EUR')].localizedName", contains("Euro (EUR)")));
    }

    @Test
    void testGetMetadataInGreekWithHeader() throws Exception {
        mockMvc.perform(get("/api/v1/expenses/metadata").locale(Locale.forLanguageTag("el")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[?(@.key == 'GROCERIES')].displayName", contains("Groceries")))
                .andExpect(jsonPath("$.categories[?(@.key == 'GROCERIES')].localizedName", contains("Σούπερ Μάρκετ")))
                .andExpect(jsonPath("$.paymentMethods[?(@.key == 'CREDIT_CARD')].localizedName", contains("Πιστωτική Κάρτα")))
                .andExpect(jsonPath("$.currencies[?(@.key == 'EUR')].localizedName", contains("Ευρώ (EUR)")));
    }

    @Test
    void testImportExpensesViaRest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "expenses.csv", "text/csv",
                "1/9/2026,Netflix,,\"8,00 €\",,,\n".getBytes()
        );

        when(expenseImportService.importExpensesFromCsv(any(MockMultipartFile.class)))
                .thenReturn(new ExpenseImportResultDto(1, 1, 0));

        mockMvc.perform(multipart("/api/v1/expenses/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expensesImported", is(1)))
                .andExpect(jsonPath("$.errorsCount", is(0)));
    }
}
