package com.example.expenses;

import com.example.expenses.config.I18nConfig;
import com.example.expenses.controller.ExpenseRestController;
import com.example.expenses.service.ExpenseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Locale;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseRestController.class)
@Import(I18nConfig.class)
class ExpenseRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseService expenseService;

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
}
