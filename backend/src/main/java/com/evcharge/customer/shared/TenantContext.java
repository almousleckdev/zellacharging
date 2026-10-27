package com.evcharge.customer.shared;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public final class TenantContext {

    private static final Pattern COUNTRY = Pattern.compile("^[A-Z]{2}$");
    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static Optional<String> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static String require() {
        return current().orElseThrow(() -> new IllegalStateException("No tenant is bound to this thread"));
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static Scope open(String country) {
        if (country == null || !COUNTRY.matcher(country).matches()) {
            throw new IllegalArgumentException("Invalid country code");
        }
        String previous = CURRENT.get();
        CURRENT.set(country);
        return () -> restore(previous);
    }

    public static <T> T callAs(String country, Supplier<T> action) {
        try (Scope scope = open(country)) {
            return action.get();
        }
    }

    public static void runAs(String country, Runnable action) {
        callAs(country, () -> {
            action.run();
            return null;
        });
    }

    private static void restore(String previous) {
        if (previous == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(previous);
        }
    }

    @FunctionalInterface
    public interface Scope extends AutoCloseable {
        @Override
        void close();
    }
}
