package com.example.expenses.controller;

import com.example.expenses.dto.ExpenseRequestDto;
import com.example.expenses.dto.ExpenseResponseDto;
import com.example.expenses.dto.MonthlySummaryDto;
import com.example.expenses.model.Category;
import com.example.expenses.model.Currency;
import com.example.expenses.model.PaymentMethod;
import com.example.expenses.service.ExpenseService;
import com.example.expenses.util.ExpenseSortUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

@Controller
public class ExpenseViewController {

    private static final Logger log = LoggerFactory.getLogger(ExpenseViewController.class);

    private final ExpenseService expenseService;
    private final MessageSource messageSource;

    public ExpenseViewController(ExpenseService expenseService, MessageSource messageSource) {
        this.expenseService = expenseService;
        this.messageSource = messageSource;
    }

    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/expenses";
    }

    @GetMapping("/expenses")
    public String showExpenses(
            @RequestParam(value = "month", required = false) String monthParam,
            @RequestParam(value = "sortBy", required = false) String sortByParam,
            @RequestParam(value = "direction", required = false) String directionParam,
            Model model,
            Locale locale) {

        YearMonth yearMonth = parseYearMonth(monthParam);
        String sortBy = ExpenseSortUtil.normalizeSortBy(sortByParam);
        String direction = ExpenseSortUtil.normalizeDirection(sortBy, directionParam);

        log.info("Serving expenses web page for month: {} (locale: {}, sortBy: {}, direction: {})",
                yearMonth, locale, sortBy, direction);
        populateModel(model, yearMonth, locale, sortBy, direction);

        if (!model.containsAttribute("newExpense")) {
            ExpenseRequestDto dto = new ExpenseRequestDto();
            dto.setDate(LocalDate.now());
            dto.setCurrency(Currency.EUR);
            model.addAttribute("newExpense", dto);
        }

        return "index";
    }

    @PostMapping("/expenses")
    public String addExpense(
            @Valid @ModelAttribute("newExpense") ExpenseRequestDto newExpense,
            BindingResult bindingResult,
            @RequestParam(value = "selectedMonth", required = false) String selectedMonth,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "direction", required = false) String direction,
            RedirectAttributes redirectAttributes,
            Model model,
            Locale locale) {

        YearMonth currentSelected = parseYearMonth(selectedMonth);

        if (bindingResult.hasErrors()) {
            log.warn("Expense form validation failed with {} errors for input: description='{}', amount={}",
                    bindingResult.getErrorCount(), newExpense.getDescription(), newExpense.getAmount());
            String validSortBy = ExpenseSortUtil.normalizeSortBy(sortBy);
            String validDirection = ExpenseSortUtil.normalizeDirection(validSortBy, direction);
            populateModel(model, currentSelected, locale, validSortBy, validDirection);
            return "index";
        }

        log.info("Web form recorded new expense: description='{}', amount={} {}, category={}, paymentMethod={}, date={}",
                newExpense.getDescription(), newExpense.getAmount(), newExpense.getCurrency(),
                newExpense.getCategory(), newExpense.getPaymentMethod(), newExpense.getDate());
        expenseService.createExpense(newExpense);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                messageSource.getMessage(
                        "message.expense.saved", null, "Expense recorded successfully!", locale));

        YearMonth expenseYearMonth = YearMonth.from(newExpense.getDate());
        return getRedirectUrl(sortBy, direction, expenseYearMonth);
    }

    @PostMapping("/expenses/{id}/delete")
    public String deleteExpense(
            @PathVariable Long id,
            @RequestParam(value = "selectedMonth", required = false) String selectedMonth,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "direction", required = false) String direction,
            RedirectAttributes redirectAttributes,
            Locale locale) {

        YearMonth currentSelected = parseYearMonth(selectedMonth);
        log.info("Web request to delete expense ID: {} (current view: {})", id, currentSelected);
        expenseService.deleteExpense(id);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                messageSource.getMessage(
                        "message.expense.deleted", null, "Expense deleted successfully!", locale));

        return getRedirectUrl(sortBy, direction, currentSelected);
    }

    private void populateModel(Model model, YearMonth yearMonth, Locale locale) {
        populateModel(model, yearMonth, locale, ExpenseSortUtil.SORT_DATE, ExpenseSortUtil.DIR_DESC);
    }

    private void populateModel(Model model, YearMonth yearMonth, Locale locale, String sortBy, String direction) {
        int year = yearMonth.getYear();
        int month = yearMonth.getMonthValue();

        MonthlySummaryDto summary = expenseService.getMonthlySummary(year, month);
        List<ExpenseResponseDto> expenses = expenseService.getExpensesByMonth(year, month);
        List<ExpenseResponseDto> sortedExpenses = ExpenseSortUtil.sort(expenses, sortBy, direction, messageSource, locale);

        model.addAttribute("expenses", sortedExpenses);
        model.addAttribute("summary", summary);
        model.addAttribute("categories", Category.values());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("currencies", Currency.values());
        model.addAttribute("defaultCurrency", Currency.EUR);
        model.addAttribute("selectedMonth", yearMonth.toString());
        model.addAttribute("previousMonth", yearMonth.minusMonths(1).toString());
        model.addAttribute("nextMonth", yearMonth.plusMonths(1).toString());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("LLLL yyyy", locale);
        String formatted = yearMonth.format(formatter);
        String currentMonthFormatted = formatted.substring(0, 1).toUpperCase(locale) + formatted.substring(1);
        model.addAttribute("currentMonthFormatted", currentMonthFormatted);
    }

    private YearMonth parseYearMonth(String monthParam) {
        if (monthParam != null && !monthParam.isBlank()) {
            try {
                return YearMonth.parse(monthParam.trim());
            } catch (DateTimeParseException ignored) {
                // fallback to current
            }
        }
        return YearMonth.now();
    }

    private static String getRedirectUrl(String sortBy, String direction, YearMonth expenseYearMonth) {
        String redirectUrl = "/expenses?month=" + expenseYearMonth;
        if (sortBy != null && !sortBy.isBlank()) {
            redirectUrl += "&sortBy=" + ExpenseSortUtil.normalizeSortBy(sortBy);
            if (direction != null && !direction.isBlank()) {
                redirectUrl += "&direction=" + ExpenseSortUtil.normalizeDirection(sortBy, direction);
            }
        }
        return "redirect:" + redirectUrl;
    }

}
