package com.example.expenses;

import com.example.expenses.dto.ExpenseImportResultDto;
import com.example.expenses.dto.ExpenseRequestDto;
import com.example.expenses.dto.ExpenseResponseDto;
import com.example.expenses.model.Category;
import com.example.expenses.model.Currency;
import com.example.expenses.model.PaymentMethod;
import com.example.expenses.service.ExpenseImportService;
import com.example.expenses.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseImportServiceTest {

    @Mock
    private ExpenseService expenseService;

    private ExpenseImportService importService;

    @BeforeEach
    void setUp() {
        importService = new ExpenseImportService(expenseService);
    }

    @Test
    void testImportUserDataFormatWithElectronicPayments() {
        String csvData = """
                Έξοδα,,Μετρητά,Ηλεκτρονικά,Ticket Restaurant,,,
                1/9/2026,Netflix,,"8,00 €",,,
                1/9/2026,Κάρτες/Παγια κινητών Vodafone - Cosmote,,"46,00 €",,,
                """;

        when(expenseService.createExpense(any(ExpenseRequestDto.class))).thenAnswer(invocation -> {
            ExpenseRequestDto req = invocation.getArgument(0);
            return new ExpenseResponseDto(1L, req.getDate(), req.getDescription(), req.getAmount(),
                    req.getCategory(), req.getPaymentMethod(), req.getCurrency());
        });

        ByteArrayInputStream in = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));
        ExpenseImportResultDto result = importService.importExpensesFromCsv(in);

        assertEquals(2, result.getTotalRowsProcessed());
        assertEquals(2, result.getExpensesImported());
        assertEquals(0, result.getErrorsCount());
        assertTrue(result.getAffectedMonths().contains("2026-09"));

        ArgumentCaptor<ExpenseRequestDto> captor = ArgumentCaptor.forClass(ExpenseRequestDto.class);
        verify(expenseService, times(2)).createExpense(captor.capture());

        List<ExpenseRequestDto> captured = captor.getAllValues();

        // 1st expense: Netflix
        ExpenseRequestDto exp1 = captured.get(0);
        assertEquals(LocalDate.of(2026, 9, 1), exp1.getDate());
        assertEquals("Netflix", exp1.getDescription());
        assertEquals(new BigDecimal("8.00"), exp1.getAmount());
        assertEquals(Category.ENTERTAINMENT, exp1.getCategory());
        assertEquals(PaymentMethod.DEBIT_CARD, exp1.getPaymentMethod());
        assertEquals(Currency.EUR, exp1.getCurrency());

        // 2nd expense: Vodafone - Cosmote
        ExpenseRequestDto exp2 = captured.get(1);
        assertEquals(LocalDate.of(2026, 9, 1), exp2.getDate());
        assertEquals("Κάρτες/Παγια κινητών Vodafone - Cosmote", exp2.getDescription());
        assertEquals(new BigDecimal("46.00"), exp2.getAmount());
        assertEquals(Category.UTILITIES, exp2.getCategory());
        assertEquals(PaymentMethod.DEBIT_CARD, exp2.getPaymentMethod());
        assertEquals(Currency.EUR, exp2.getCurrency());
    }

    @Test
    void testImportWithCashAndTicketRestaurant() {
        String csvData = """
                Έξοδα,,Μετρητά,Ηλεκτρονικά,Ticket Restaurant,,,
                02/09/2026,Σούπερ Μάρκετ Σκλαβενίτης,"35,50 €",,,,,
                03/09/2026,Efood Παραγγελία,,,"12,00 €",,,
                """;

        when(expenseService.createExpense(any(ExpenseRequestDto.class))).thenAnswer(invocation -> {
            ExpenseRequestDto req = invocation.getArgument(0);
            return new ExpenseResponseDto(1L, req.getDate(), req.getDescription(), req.getAmount(),
                    req.getCategory(), req.getPaymentMethod(), req.getCurrency());
        });

        ByteArrayInputStream in = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));
        ExpenseImportResultDto result = importService.importExpensesFromCsv(in);

        assertEquals(2, result.getExpensesImported());
        assertEquals(0, result.getErrorsCount());

        ArgumentCaptor<ExpenseRequestDto> captor = ArgumentCaptor.forClass(ExpenseRequestDto.class);
        verify(expenseService, times(2)).createExpense(captor.capture());

        List<ExpenseRequestDto> captured = captor.getAllValues();

        // Cash expense -> Groceries
        ExpenseRequestDto exp1 = captured.get(0);
        assertEquals(PaymentMethod.CASH, exp1.getPaymentMethod());
        assertEquals(new BigDecimal("35.50"), exp1.getAmount());
        assertEquals(Category.GROCERIES, exp1.getCategory());

        // Ticket Restaurant -> Gift Card, Food & Dining
        ExpenseRequestDto exp2 = captured.get(1);
        assertEquals(PaymentMethod.GIFT_CARD, exp2.getPaymentMethod());
        assertEquals(new BigDecimal("12.00"), exp2.getAmount());
        assertEquals(Category.FOOD_AND_DINING, exp2.getCategory());
    }

    @Test
    void testImportSemicolonDelimiterAndUtf8Bom() {
        String csvData = "\uFEFFΈξοδα;;Μετρητά;Ηλεκτρονικά;Ticket Restaurant\n" +
                "15/09/2026;Βενζίνη Shell;;60,00 €;\n";

        when(expenseService.createExpense(any(ExpenseRequestDto.class))).thenAnswer(invocation -> {
            ExpenseRequestDto req = invocation.getArgument(0);
            return new ExpenseResponseDto(1L, req.getDate(), req.getDescription(), req.getAmount(),
                    req.getCategory(), req.getPaymentMethod(), req.getCurrency());
        });

        ByteArrayInputStream in = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));
        ExpenseImportResultDto result = importService.importExpensesFromCsv(in);

        assertEquals(1, result.getExpensesImported());
        ArgumentCaptor<ExpenseRequestDto> captor = ArgumentCaptor.forClass(ExpenseRequestDto.class);
        verify(expenseService).createExpense(captor.capture());

        ExpenseRequestDto exp = captor.getValue();
        assertEquals("Βενζίνη Shell", exp.getDescription());
        assertEquals(new BigDecimal("60.00"), exp.getAmount());
        assertEquals(Category.TRANSPORTATION, exp.getCategory());
        assertEquals(PaymentMethod.DEBIT_CARD, exp.getPaymentMethod());
    }

    @Test
    void testImportEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", new byte[0]);
        ExpenseImportResultDto result = importService.importExpensesFromCsv(file);

        assertEquals(0, result.getExpensesImported());
        assertEquals(1, result.getErrorsCount());
        assertTrue(result.getErrorMessages().get(0).contains("empty"));
    }

    @Test
    void testCategoryInferenceDefaultsToOther() {
        assertEquals(Category.OTHER, importService.inferCategory("Random non-matching description 12345"));
        assertEquals(Category.OTHER, importService.inferCategory(null));
        assertEquals(Category.OTHER, importService.inferCategory("   "));
    }

    @Test
    void testMatchesAnyCaseInsensitive() {
        assertTrue(ExpenseImportService.matchesAny("NETFLIX SUBSCRIPTION", "netflix"));
        assertTrue(ExpenseImportService.matchesAny("netflix subscription", "NETFLIX"));
        assertTrue(ExpenseImportService.matchesAny("My Spotify Account", "sPoTiFy"));
        assertTrue(ExpenseImportService.matchesAny("Cosmote Payment", "COSMOTE", "VODAFONE"));
        assertFalse(ExpenseImportService.matchesAny("Random text", "NETFLIX", "SPOTIFY"));
        assertFalse(ExpenseImportService.matchesAny(null, "test"));
        assertFalse(ExpenseImportService.matchesAny("test", (String[]) null));
        assertFalse(ExpenseImportService.matchesAny("test", (String) null));
    }
}
