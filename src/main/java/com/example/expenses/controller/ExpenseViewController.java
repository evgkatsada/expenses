package com.example.expenses.controller;

import com.example.expenses.dto.ExpenseRequestDto;
import com.example.expenses.dto.MonthlySummaryDto;
import com.example.expenses.model.Category;
import com.example.expenses.model.Currency;
import com.example.expenses.model.PaymentMethod;
import com.example.expenses.service.ExpenseService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

@Controller
public class ExpenseViewController {

    private static final Logger log = LoggerFactory.getLogger(ExpenseViewController.class);

    private final ExpenseService expenseService;

    public ExpenseViewController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/expenses";
    }

    @GetMapping("/expenses")
    public String showExpenses(
            @RequestParam(value = "month", required = false) String monthParam,
            Model model) {

        YearMonth yearMonth = parseYearMonth(monthParam);
        log.info("Serving expenses web page for month: {}", yearMonth);
        populateModel(model, yearMonth);

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
            RedirectAttributes redirectAttributes,
            Model model) {

        YearMonth currentSelected = parseYearMonth(selectedMonth);

        if (bindingResult.hasErrors()) {
            log.warn("Expense form validation failed with {} errors for input: description='{}', amount={}",
                    bindingResult.getErrorCount(), newExpense.getDescription(), newExpense.getAmount());
            populateModel(model, currentSelected);
            return "index";
        }

        log.info("Web form recorded new expense: description='{}', amount={} {}, category={}, paymentMethod={}, date={}",
                newExpense.getDescription(), newExpense.getAmount(), newExpense.getCurrency(),
                newExpense.getCategory(), newExpense.getPaymentMethod(), newExpense.getDate());
        expenseService.createExpense(newExpense);
        redirectAttributes.addFlashAttribute("successMessage", "Expense recorded successfully!");

        YearMonth expenseYearMonth = YearMonth.from(newExpense.getDate());
        return "redirect:/expenses?month=" + expenseYearMonth;
    }

    @PostMapping("/expenses/{id}/delete")
    public String deleteExpense(
            @PathVariable Long id,
            @RequestParam(value = "selectedMonth", required = false) String selectedMonth,
            RedirectAttributes redirectAttributes) {

        YearMonth currentSelected = parseYearMonth(selectedMonth);
        log.info("Web request to delete expense ID: {} (current view: {})", id, currentSelected);
        expenseService.deleteExpense(id);
        redirectAttributes.addFlashAttribute("successMessage", "Expense deleted successfully!");

        return "redirect:/expenses?month=" + currentSelected;
    }

    private void populateModel(Model model, YearMonth yearMonth) {
        int year = yearMonth.getYear();
        int month = yearMonth.getMonthValue();

        MonthlySummaryDto summary = expenseService.getMonthlySummary(year, month);

        model.addAttribute("expenses", expenseService.getExpensesByMonth(year, month));
        model.addAttribute("summary", summary);
        model.addAttribute("categories", Category.values());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("currencies", Currency.values());
        model.addAttribute("defaultCurrency", Currency.EUR);
        model.addAttribute("selectedMonth", yearMonth.toString());
        model.addAttribute("previousMonth", yearMonth.minusMonths(1).toString());
        model.addAttribute("nextMonth", yearMonth.plusMonths(1).toString());
        model.addAttribute("currentMonthFormatted", yearMonth.getMonth().name() + " " + yearMonth.getYear());
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
}
