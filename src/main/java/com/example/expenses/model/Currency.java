package com.example.expenses.model;

public enum Currency {
    EUR("€", "Euro (EUR)"),
    USD("$", "US Dollar (USD)"),
    GBP("£", "British Pound (GBP)"),
    CHF("CHF", "Swiss Franc (CHF)"),
    JPY("¥", "Japanese Yen (JPY)"),
    CAD("CA$", "Canadian Dollar (CAD)"),
    AUD("AU$", "Australian Dollar (AUD)");

    private final String symbol;
    private final String displayName;

    Currency(String symbol, String displayName) {
        this.symbol = symbol;
        this.displayName = displayName;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDisplayName() {
        return displayName;
    }
}
