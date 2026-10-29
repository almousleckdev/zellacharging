package com.evcharge.customer.shared;

import java.util.Map;
import java.util.Set;

public record Sort(String field, boolean descending) {

    public static Sort parse(String raw, Set<String> allowed, String fallback) {
        if (raw == null || raw.isBlank()) {
            return new Sort(fallback, true);
        }
        String[] parts = raw.split(":", 2);
        String field = allowed.contains(parts[0]) ? parts[0] : fallback;
        boolean descending = parts.length < 2 || !"asc".equalsIgnoreCase(parts[1]);
        return new Sort(field, descending);
    }

    public String orderBy(Map<String, String> columns, String tieBreaker) {
        return " order by " + columns.get(field) + (descending ? " desc" : " asc") + " nulls last, " + tieBreaker;
    }
}
