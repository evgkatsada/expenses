package com.example.expenses.service;

import com.example.expenses.dto.ExpenseImportResultDto;
import com.example.expenses.dto.ExpenseRequestDto;
import com.example.expenses.dto.ExpenseResponseDto;
import com.example.expenses.model.Category;
import com.example.expenses.model.Currency;
import com.example.expenses.model.PaymentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class ExpenseImportService {

    private static final Logger log = LoggerFactory.getLogger(ExpenseImportService.class);

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d-M-yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("d.M.yyyy"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("d/M/yy"),
            DateTimeFormatter.ofPattern("dd/MM/yy")
    );

    private final ExpenseService expenseService;

    public ExpenseImportService(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    public ExpenseImportResultDto importExpensesFromCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            ExpenseImportResultDto emptyResult = new ExpenseImportResultDto(0, 0, 1);
            emptyResult.getErrorMessages().add("The uploaded file is empty.");
            return emptyResult;
        }

        try (InputStream inputStream = file.getInputStream()) {
            return importExpensesFromCsv(inputStream);
        } catch (Exception e) {
            log.error("Failed to read uploaded CSV file", e);
            ExpenseImportResultDto errorResult = new ExpenseImportResultDto(0, 0, 1);
            errorResult.getErrorMessages().add("Failed to read file: " + e.getMessage());
            return errorResult;
        }
    }

    public ExpenseImportResultDto importExpensesFromCsv(InputStream inputStream) {
        ExpenseImportResultDto result = new ExpenseImportResultDto();

        if (inputStream == null) {
            result.setErrorsCount(1);
            result.getErrorMessages().add("No input stream provided.");
            return result;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            List<String> rawLines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                // Strip UTF-8 Byte Order Mark (BOM) if present
                if (rawLines.isEmpty() && line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
                if (!line.trim().isEmpty()) {
                    rawLines.add(line);
                }
            }

            if (rawLines.isEmpty()) {
                result.setErrorsCount(1);
                result.getErrorMessages().add("The file contains no data.");
                return result;
            }

            char delimiter = detectDelimiter(rawLines.get(0));
            int startIndex = 0;

            // Default column indexes: Date=0, Description=1, Cash=2, Electronic=3, Ticket Restaurant=4
            int dateIdx = 0;
            int descIdx = 1;
            int cashIdx = 2;
            int elecIdx = 3;
            int trIdx = 4;

            // Check if first line is a header
            if (isHeaderLine(rawLines.get(0), delimiter)) {
                List<String> headerTokens = parseCsvLine(rawLines.get(0), delimiter);
                for (int i = 0; i < headerTokens.size(); i++) {
                    String norm = stripAccents(headerTokens.get(i).toLowerCase(Locale.ROOT));
                    if (norm.contains("μετρητ") || norm.contains("cash")) {
                        cashIdx = i;
                    } else if (norm.contains("ηλεκτρον") || norm.contains("electronic") || norm.contains("καρτ") || norm.contains("card")) {
                        elecIdx = i;
                    } else if (norm.contains("ticket") || norm.contains("restaurant")) {
                        trIdx = i;
                    } else if (norm.contains("ημερομην") || norm.contains("date") || norm.contains("εξοδ")) {
                        dateIdx = i;
                    } else if (norm.contains("περιγραφ") || norm.contains("description")) {
                        descIdx = i;
                    }
                }
                startIndex = 1;
            }

            for (int lineNum = startIndex; lineNum < rawLines.size(); lineNum++) {
                String currentLine = rawLines.get(lineNum);
                result.setTotalRowsProcessed(result.getTotalRowsProcessed() + 1);

                List<String> tokens = parseCsvLine(currentLine, delimiter);
                if (tokens.isEmpty() || isAllEmpty(tokens)) {
                    continue;
                }

                if (tokens.size() <= Math.max(dateIdx, descIdx)) {
                    result.setErrorsCount(result.getErrorsCount() + 1);
                    result.getErrorMessages().add("Line " + (lineNum + 1) + ": Not enough columns.");
                    continue;
                }

                String dateStr = tokens.get(dateIdx);
                LocalDate date = parseDate(dateStr);
                if (date == null) {
                    result.setErrorsCount(result.getErrorsCount() + 1);
                    result.getErrorMessages().add("Line " + (lineNum + 1) + ": Invalid date format '" + dateStr + "'");
                    continue;
                }

                String description = tokens.get(descIdx);
                if (description.isBlank()) {
                    description = "Imported expense";
                }

                Category category = inferCategory(description);

                boolean expenseSavedForLine = false;

                // 1. Cash Column -> PaymentMethod.CASH
                if (cashIdx >= 0 && cashIdx < tokens.size()) {
                    BigDecimal cashAmount = parseAmount(tokens.get(cashIdx));
                    if (cashAmount != null) {
                        Currency currency = parseCurrency(tokens.get(cashIdx));
                        createAndRecordExpense(date, description, cashAmount, category, PaymentMethod.CASH, currency, result);
                        expenseSavedForLine = true;
                    }
                }

                // 2. Electronic Column -> PaymentMethod.DEBIT_CARD
                if (elecIdx >= 0 && elecIdx < tokens.size()) {
                    BigDecimal elecAmount = parseAmount(tokens.get(elecIdx));
                    if (elecAmount != null) {
                        Currency currency = parseCurrency(tokens.get(elecIdx));
                        createAndRecordExpense(date, description, elecAmount, category, PaymentMethod.DEBIT_CARD, currency, result);
                        expenseSavedForLine = true;
                    }
                }

                // 3. Ticket Restaurant Column -> PaymentMethod.GIFT_CARD
                if (trIdx >= 0 && trIdx < tokens.size()) {
                    BigDecimal trAmount = parseAmount(tokens.get(trIdx));
                    if (trAmount != null) {
                        Currency currency = parseCurrency(tokens.get(trIdx));
                        createAndRecordExpense(date, description, trAmount, category, PaymentMethod.GIFT_CARD, currency, result);
                        expenseSavedForLine = true;
                    }
                }

                if (!expenseSavedForLine) {
                    result.setErrorsCount(result.getErrorsCount() + 1);
                    result.getErrorMessages().add("Line " + (lineNum + 1) + ": No valid expense amount found for '" + description + "'");
                }
            }

        } catch (Exception e) {
            log.error("Failed to parse CSV stream", e);
            result.setErrorsCount(result.getErrorsCount() + 1);
            result.getErrorMessages().add("Error processing CSV: " + e.getMessage());
        }

        return result;
    }

    private void createAndRecordExpense(
            LocalDate date,
            String description,
            BigDecimal amount,
            Category category,
            PaymentMethod paymentMethod,
            Currency currency,
            ExpenseImportResultDto result) {

        ExpenseRequestDto dto = new ExpenseRequestDto();
        dto.setDate(date);
        dto.setDescription(description);
        dto.setAmount(amount);
        dto.setCategory(category);
        dto.setPaymentMethod(paymentMethod);
        dto.setCurrency(currency);

        ExpenseResponseDto saved = expenseService.createExpense(dto);
        result.setExpensesImported(result.getExpensesImported() + 1);
        result.getImportedExpenses().add(saved);
        result.getAffectedMonths().add(YearMonth.from(date).toString());
    }

    public char detectDelimiter(String headerLine) {
        long commas = headerLine.chars().filter(ch -> ch == ',').count();
        long semicolons = headerLine.chars().filter(ch -> ch == ';').count();
        return semicolons > commas ? ';' : ',';
    }

    public List<String> parseCsvLine(String line, char delimiter) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == delimiter && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens;
    }

    public boolean isHeaderLine(String line, char delimiter) {
        List<String> tokens = parseCsvLine(line, delimiter);
        if (tokens.isEmpty()) {
            return false;
        }
        String first = stripAccents(tokens.get(0).toLowerCase(Locale.ROOT));
        if (first.contains("εξοδ") || first.contains("date") || first.contains("ημερομην") || first.contains("expense")) {
            return true;
        }
        for (String t : tokens) {
            String norm = stripAccents(t.toLowerCase(Locale.ROOT));
            if (norm.contains("μετρητ") || norm.contains("ηλεκτρον") || norm.contains("ticket restaurant") || norm.contains("cash")) {
                return true;
            }
        }
        return parseDate(tokens.get(0)) == null;
    }

    public LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        String cleaned = dateStr.replace("\"", "").trim();
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(cleaned, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    public BigDecimal parseAmount(String amountStr) {
        if (amountStr == null || amountStr.isBlank()) {
            return null;
        }
        String raw = amountStr.replace("\"", "").trim();
        StringBuilder digitsAndSeparators = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (Character.isDigit(c) || c == ',' || c == '.' || c == '-') {
                digitsAndSeparators.append(c);
            }
        }
        String cleaned = digitsAndSeparators.toString().trim();
        if (cleaned.isEmpty() || cleaned.equals("-")) {
            return null;
        }

        if (cleaned.contains(".") && cleaned.contains(",")) {
            int dotIdx = cleaned.indexOf('.');
            int commaIdx = cleaned.indexOf(',');
            if (dotIdx < commaIdx) {
                cleaned = cleaned.replace(".", "").replace(',', '.');
            } else {
                cleaned = cleaned.replace(",", "");
            }
        } else if (cleaned.contains(",")) {
            cleaned = cleaned.replace(',', '.');
        }

        try {
            BigDecimal amount = new BigDecimal(cleaned);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return null;
            }
            return amount;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Currency parseCurrency(String amountStr) {
        if (amountStr == null || amountStr.isBlank()) {
            return Currency.EUR;
        }
        String upper = amountStr.toUpperCase(Locale.ROOT);
        if (upper.contains("$") || upper.contains("USD")) {
            return Currency.USD;
        }
        if (upper.contains("£") || upper.contains("GBP")) {
            return Currency.GBP;
        }
        if (upper.contains("CHF")) {
            return Currency.CHF;
        }
        if (upper.contains("¥") || upper.contains("JPY")) {
            return Currency.JPY;
        }
        if (upper.contains("CA$") || upper.contains("CAD")) {
            return Currency.CAD;
        }
        if (upper.contains("AU$") || upper.contains("AUD")) {
            return Currency.AUD;
        }
        return Currency.EUR;
    }

    public Category inferCategory(String description) {
        if (description == null || description.isBlank()) {
            return Category.OTHER;
        }
        String normalized = stripAccents(description.toLowerCase(Locale.ROOT));

        // Entertainment
        if (matchesAny(normalized, "netflix", "spotify", "cinema", "σινεμα", "θεατρ", "theatre", "theater",
                "συναυλι", "steam", "playstation", "nintendo", "youtube", "disney", "hbo", "apple tv", "gaming")) {
            return Category.ENTERTAINMENT;
        }

        // Utilities
        if (matchesAny(normalized, "vodafone", "cosmote", "nova", "wind", "κινητ", "παγι", "δεη", "dei", "ρευμα",
                "ευδαπ", "νερο", "αεριο", "φυσικο αεριο", "κοινοχρηστ", "ιντερνετ", "internet", "bill", "ote")) {
            return Category.UTILITIES;
        }

        // Groceries
        if (matchesAny(normalized, "σουπερ μαρκετ", "supermarket", "market", "lidl", "βασιλοπουλος", "αβ ", "ab ",
                "σκλαβενιτης", "μασουτης", "κρητικος", "γαλακτοπωλ", "μαναβικ", "κρεοπωλ", "ιχθυοπωλ", "φουρν",
                "αρτοποι", "μπακαλικ", "παντοπωλ")) {
            return Category.GROCERIES;
        }

        // Food & Dining
        if (matchesAny(normalized, "φαγητο", "εστιατορι", "ταβερν", "σουβλακ", "πιτσ", "pizza", "καφε", "coffee",
                "cafe", "delivery", "wolt", "efood", "mcdonald", "kfc", "burger", "μπαρ", "bar", "ποτο", "snack")) {
            return Category.FOOD_AND_DINING;
        }

        // Transportation
        if (matchesAny(normalized, "βενζιν", "καυσιμ", "gas", "fuel", "shell", "bp", "eko", "avin", "διοδι",
                "μετρο", "λεωφορει", "ταξι", "taxi", "uber", "freenow", "παρκινγκ", "parking", "κτελ", "aegean",
                "ryanair", "εισητηρι", "ticket", "διοδια")) {
            return Category.TRANSPORTATION;
        }

        // Healthcare
        if (matchesAny(normalized, "φαρμακει", "pharmacy", "γιατρ", "ιατρ", "doctor", "νοσοκομ", "εξετασ",
                "οδοντιατρ", "ορθοδοντ", "dentist", "φαρμακ", "medical", "health")) {
            return Category.HEALTHCARE;
        }

        // Shopping
        if (matchesAny(normalized, "ρουχ", "παπουτσι", "clothes", "shoes", "zara", "h&m", "attica", "jumbo",
                "ikea", "leroy", "πλαισιο", "plaisio", "public", "skroutz", "amazon", "καλλυντικ", "hondos", "mall")) {
            return Category.SHOPPING;
        }

        return Category.OTHER;
    }

    private static String stripAccents(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    public static boolean matchesAny(String text, String... keywords) {
        if (text == null || keywords == null) {
            return false;
        }
        String lowerText = text.toLowerCase(Locale.ROOT);
        for (String kw : keywords) {
            if (kw != null && lowerText.contains(kw.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAllEmpty(List<String> tokens) {
        for (String t : tokens) {
            if (!t.isBlank()) {
                return false;
            }
        }
        return true;
    }
}
