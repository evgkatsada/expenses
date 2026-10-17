package com.example.expenses.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ExpenseImportResultDto {

    private int totalRowsProcessed;
    private int expensesImported;
    private int errorsCount;
    private List<String> errorMessages = new ArrayList<>();
    private List<ExpenseResponseDto> importedExpenses = new ArrayList<>();
    private Set<String> affectedMonths = new TreeSet<>();

    public ExpenseImportResultDto() {
    }

    public ExpenseImportResultDto(int totalRowsProcessed, int expensesImported, int errorsCount) {
        this.totalRowsProcessed = totalRowsProcessed;
        this.expensesImported = expensesImported;
        this.errorsCount = errorsCount;
    }

    public int getTotalRowsProcessed() {
        return totalRowsProcessed;
    }

    public void setTotalRowsProcessed(int totalRowsProcessed) {
        this.totalRowsProcessed = totalRowsProcessed;
    }

    public int getExpensesImported() {
        return expensesImported;
    }

    public void setExpensesImported(int expensesImported) {
        this.expensesImported = expensesImported;
    }

    public int getErrorsCount() {
        return errorsCount;
    }

    public void setErrorsCount(int errorsCount) {
        this.errorsCount = errorsCount;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public List<ExpenseResponseDto> getImportedExpenses() {
        return importedExpenses;
    }

    public void setImportedExpenses(List<ExpenseResponseDto> importedExpenses) {
        this.importedExpenses = importedExpenses;
    }

    public Set<String> getAffectedMonths() {
        return affectedMonths;
    }

    public void setAffectedMonths(Set<String> affectedMonths) {
        this.affectedMonths = affectedMonths;
    }
}
