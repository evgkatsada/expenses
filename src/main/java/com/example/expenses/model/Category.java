package com.example.expenses.model;

public enum Category {
    GROCERIES("Groceries"),
    FOOD_AND_DINING("Food & Dining"),
    UTILITIES("Utilities"),
    TRANSPORTATION("Transportation"),
    ENTERTAINMENT("Entertainment"),
    HEALTHCARE("Healthcare"),
    SHOPPING("Shopping"),
    OTHER("Other");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
