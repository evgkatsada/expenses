package com.example.expenses.service;

import com.example.expenses.dto.ExpenseRequestDto;
import com.example.expenses.dto.ExpenseResponseDto;
import com.example.expenses.dto.MonthlySummaryDto;
import com.example.expenses.model.Category;
import com.example.expenses.model.Currency;
import com.example.expenses.model.Expense;
import com.example.expenses.model.PaymentMethod;
import com.example.expenses.repository.ExpenseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExpenseService {

    private static final Logger log = LoggerFactory.getLogger(ExpenseService.class);

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public ExpenseResponseDto createExpense(ExpenseRequestDto requestDto) {
        log.info("Creating new expense: description='{}', amount={} {}, category={}, paymentMethod={}, date={}",
                requestDto.getDescription(), requestDto.getAmount(), requestDto.getCurrency(),
                requestDto.getCategory(), requestDto.getPaymentMethod(), requestDto.getDate());
        Expense expense = toEntity(requestDto);
        Expense saved = expenseRepository.save(expense);
        log.info("Successfully created expense with ID: {}", saved.getId());
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public ExpenseResponseDto getExpenseById(Long id) {
        log.info("Fetching expense with ID: {}", id);
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Expense not found with ID: " + id));
        return toDto(expense);
    }

    public ExpenseResponseDto updateExpense(Long id, ExpenseRequestDto requestDto) {
        log.info("Updating expense with ID: {}", id);
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Expense not found with ID: " + id));

        expense.setDate(requestDto.getDate());
        expense.setDescription(requestDto.getDescription());
        expense.setAmount(requestDto.getAmount());
        expense.setCategory(requestDto.getCategory());
        expense.setPaymentMethod(requestDto.getPaymentMethod());
        expense.setCurrency(requestDto.getCurrency());

        Expense updated = expenseRepository.save(expense);
        log.info("Successfully updated expense with ID: {}", updated.getId());
        return toDto(updated);
    }

    public void deleteExpense(Long id) {
        log.info("Deleting expense with ID: {}", id);
        if (!expenseRepository.existsById(id)) {
            throw new NoSuchElementException("Expense not found with ID: " + id);
        }
        expenseRepository.deleteById(id);
        log.info("Successfully deleted expense with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponseDto> getAllExpenses() {
        log.info("Fetching all expenses");
        return expenseRepository.findAllByOrderByDateDescIdDesc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponseDto> getExpensesByMonth(int year, int month) {
        log.info("Fetching expenses for {}-{}", year, String.format("%02d", month));
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        return expenseRepository.findByDateBetweenOrderByDateDescIdDesc(startDate, endDate)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MonthlySummaryDto getMonthlySummary(int year, int month) {
        log.info("Calculating monthly summary for {}-{}", year, String.format("%02d", month));
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Expense> expenses = expenseRepository.findByDateBetweenOrderByDateDescIdDesc(startDate, endDate);

        BigDecimal totalAmount = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Category, BigDecimal> categoryBreakdown = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        () -> new EnumMap<>(Category.class),
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        Map<PaymentMethod, BigDecimal> paymentMethodBreakdown = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getPaymentMethod,
                        () -> new EnumMap<>(PaymentMethod.class),
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        Map<Currency, BigDecimal> currencyBreakdown = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCurrency,
                        () -> new EnumMap<>(Currency.class),
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        log.info("Monthly summary calculated for {}-{}: {} expenses found, total={}",
                year, String.format("%02d", month), expenses.size(), totalAmount);

        return new MonthlySummaryDto(
                year,
                month,
                totalAmount,
                expenses.size(),
                categoryBreakdown,
                paymentMethodBreakdown,
                currencyBreakdown
        );
    }

    public ExpenseResponseDto toDto(Expense entity) {
        return new ExpenseResponseDto(
                entity.getId(),
                entity.getDate(),
                entity.getDescription(),
                entity.getAmount(),
                entity.getCategory(),
                entity.getPaymentMethod(),
                entity.getCurrency()
        );
    }

    public Expense toEntity(ExpenseRequestDto dto) {
        return new Expense(
                dto.getDate(),
                dto.getDescription(),
                dto.getAmount(),
                dto.getCategory(),
                dto.getPaymentMethod(),
                dto.getCurrency()
        );
    }
}
