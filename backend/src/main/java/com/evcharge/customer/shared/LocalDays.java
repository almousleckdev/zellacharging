package com.evcharge.customer.shared;

public final class LocalDays {

    private static final String ZONE = "(select time_zone from country where code = current_setting('app.country', true))";

    private LocalDays() {
    }

    public static String bucket(String column, Bucket bucket) {
        return "date_trunc('" + bucket.truncation() + "', " + column + " at time zone " + ZONE + ")::date";
    }
}
