package com.evcharge.customer.shared;

public enum Bucket {
    DAY("day"),
    WEEK("week"),
    MONTH("month");

    private final String truncation;

    Bucket(String truncation) {
        this.truncation = truncation;
    }

    public String truncation() {
        return truncation;
    }

    public static Bucket parse(String value) {
        if (value == null || value.isBlank()) {
            return DAY;
        }
        return valueOf(value.trim().toUpperCase());
    }
}
