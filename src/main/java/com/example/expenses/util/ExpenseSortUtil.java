package com.example.expenses.util;

import com.example.expenses.dto.ExpenseResponseDto;
import com.example.expenses.model.Category;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.text.Collator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class ExpenseSortUtil {

    public static final String SORT_DATE = "date";
    public static final String SORT_AMOUNT = "amount";
    public static final String SORT_CATEGORY = "category";

    public static final String DIR_ASC = "asc";
    public static final String DIR_DESC = "desc";

    private ExpenseSortUtil() {
    }

    public static String normalizeSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return SORT_DATE;
        }
        String normalized = sortBy.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case SORT_AMOUNT -> SORT_AMOUNT;
            case SORT_CATEGORY -> SORT_CATEGORY;
            default -> SORT_DATE;
        };
    }

    public static String normalizeDirection(String sortBy, String direction) {
        String normalizedSort = normalizeSortBy(sortBy);
        if (direction != null && !direction.isBlank()) {
            String dir = direction.trim().toLowerCase(Locale.ROOT);
            if (DIR_ASC.equals(dir) || DIR_DESC.equals(dir)) {
                return dir;
            }
        }
        return SORT_CATEGORY.equals(normalizedSort) ? DIR_ASC : DIR_DESC;
    }

    public static List<ExpenseResponseDto> sort(
            List<ExpenseResponseDto> expenses,
            String sortBy,
            String direction,
            MessageSource messageSource,
            Locale locale) {

        if (expenses == null || expenses.isEmpty()) {
            return expenses != null ? new ArrayList<>(expenses) : Collections.emptyList();
        }

        String validSortBy = normalizeSortBy(sortBy);
        String validDir = normalizeDirection(validSortBy, direction);
        boolean isAsc = DIR_ASC.equals(validDir);

        List<ExpenseResponseDto> list = new ArrayList<>(expenses);
        Comparator<ExpenseResponseDto> comparator;

        switch (validSortBy) {
            case SORT_AMOUNT:
                comparator = Comparator.comparing(
                        ExpenseResponseDto::getAmount,
                        Comparator.nullsLast(BigDecimal::compareTo)
                );
                if (!isAsc) {
                    comparator = comparator.reversed();
                }
                comparator = comparator
                        .thenComparing(ExpenseResponseDto::getDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ExpenseResponseDto::getId, Comparator.nullsLast(Comparator.reverseOrder()));
                break;

            case SORT_CATEGORY:
                Locale effectiveLocale = locale != null ? locale : Locale.ENGLISH;
                Collator collator = Collator.getInstance(effectiveLocale);
                comparator = (e1, e2) -> {
                    String cat1 = resolveCategoryLabel(e1, messageSource, effectiveLocale);
                    String cat2 = resolveCategoryLabel(e2, messageSource, effectiveLocale);
                    int res = collator.compare(cat1, cat2);
                    return isAsc ? res : -res;
                };
                comparator = comparator
                        .thenComparing(ExpenseResponseDto::getDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ExpenseResponseDto::getId, Comparator.nullsLast(Comparator.reverseOrder()));
                break;

            case SORT_DATE:
            default:
                comparator = Comparator.comparing(
                        ExpenseResponseDto::getDate,
                        Comparator.nullsLast(LocalDate::compareTo)
                );
                if (!isAsc) {
                    comparator = comparator.reversed();
                }
                Comparator<Long> idOrder = isAsc ? Comparator.naturalOrder() : Comparator.reverseOrder();
                comparator = comparator
                        .thenComparing(ExpenseResponseDto::getId, Comparator.nullsLast(idOrder));
                break;
        }

        list.sort(comparator);
        return list;
    }

    private static String resolveCategoryLabel(ExpenseResponseDto dto, MessageSource messageSource, Locale locale) {
        if (dto == null || dto.getCategory() == null) {
            return "";
        }
        Category cat = dto.getCategory();
        if (messageSource != null) {
            try {
                return messageSource.getMessage("category." + cat.name(), null, cat.getDisplayName(), locale);
            } catch (Exception ignored) {
            }
        }
        return cat.getDisplayName();
    }
}
