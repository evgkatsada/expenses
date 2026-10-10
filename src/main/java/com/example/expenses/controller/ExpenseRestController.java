package com.example.expenses.controller;

import com.example.expenses.dto.ExpenseRequestDto;
import com.example.expenses.dto.ExpenseResponseDto;
import com.example.expenses.dto.MonthlySummaryDto;
import com.example.expenses.model.Category;
import com.example.expenses.model.PaymentMethod;
import com.example.expenses.service.ExpenseService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/expenses")
public class ExpenseRestController {

    private static final Logger log = LoggerFactory.getLogger(ExpenseRestController.class);

    private final ExpenseService expenseService;

    public ExpenseRestController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponseDto>> getExpenses(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        log.info("REST GET /api/v1/expenses requested (year={}, month={})", year, month);
        if (year != null && month != null) {
            return ResponseEntity.ok(expenseService.getExpensesByMonth(year, month));
        }
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponseDto> getExpenseById(@PathVariable Long id) {
        log.info("REST GET /api/v1/expenses/{} requested", id);
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    @PostMapping
    public ResponseEntity<ExpenseResponseDto> createExpense(@Valid @RequestBody ExpenseRequestDto requestDto) {
        log.info("REST POST /api/v1/expenses requested: description='{}', amount={}",
                requestDto.getDescription(), requestDto.getAmount());
        ExpenseResponseDto created = expenseService.createExpense(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponseDto> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequestDto requestDto) {
        log.info("REST PUT /api/v1/expenses/{} requested", id);
        ExpenseResponseDto updated = expenseService.updateExpense(id, requestDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        log.info("REST DELETE /api/v1/expenses/{} requested", id);
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<MonthlySummaryDto> getMonthlySummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        LocalDate now = LocalDate.now();
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();
        log.info("REST GET /api/v1/expenses/summary requested for year={}, month={}", targetYear, targetMonth);
        return ResponseEntity.ok(expenseService.getMonthlySummary(targetYear, targetMonth));
    }

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        log.info("REST GET /api/v1/expenses/metadata requested");
        Map<String, Object> metadata = new HashMap<>();

        List<Map<String, String>> categories = Arrays.stream(Category.values())
                .map(c -> Map.of("key", c.name(), "displayName", c.getDisplayName()))
                .toList();

        List<Map<String, String>> paymentMethods = Arrays.stream(PaymentMethod.values())
                .map(p -> Map.of("key", p.name(), "displayName", p.getDisplayName()))
                .toList();

        metadata.put("categories", categories);
        metadata.put("paymentMethods", paymentMethods);
        return ResponseEntity.ok(metadata);
    }
}
