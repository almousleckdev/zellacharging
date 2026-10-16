package com.evcharge.customer.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(long minor, String currency, int decimals) {

    private static final int MAX_DECIMALS = 3;

    public Money {
        Objects.requireNonNull(currency, "currency");
        if (currency.length() != 3 || !currency.equals(currency.toUpperCase(java.util.Locale.ROOT))) {
            throw new IllegalArgumentException("currency must be an upper-case ISO 4217 code: " + currency);
        }
        if (decimals < 0 || decimals > MAX_DECIMALS) {
            throw new IllegalArgumentException("decimals must be between 0 and " + MAX_DECIMALS);
        }
        if (minor < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
    }

    public static Money zero(String currency, int decimals) {
        return new Money(0, currency, decimals);
    }

        public static Money forEnergy(BigDecimal kwh, long pricePerKwhMinor, String currency, int decimals) {
        if (kwh.signum() < 0 || pricePerKwhMinor < 0) {
            throw new IllegalArgumentException("energy and price must not be negative");
        }
        long minor = kwh.multiply(BigDecimal.valueOf(pricePerKwhMinor))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
        return new Money(minor, currency, decimals);
    }

        public BigDecimal energyAt(long pricePerKwhMinor) {
        if (pricePerKwhMinor <= 0) {
            throw new IllegalArgumentException("price per kWh must be positive");
        }
        return BigDecimal.valueOf(minor).divide(BigDecimal.valueOf(pricePerKwhMinor), 3, RoundingMode.DOWN);
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(Math.addExact(minor, other.minor), currency, decimals);
    }

        public Money minus(Money other) {
        requireSameCurrency(other);
        return new Money(minor - other.minor, currency, decimals);
    }

    public Money min(Money other) {
        requireSameCurrency(other);
        return minor <= other.minor ? this : other;
    }

    public boolean isZero() {
        return minor == 0;
    }

        public BigDecimal toMajor() {
        return BigDecimal.valueOf(minor, decimals);
    }

    private void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency) || decimals != other.decimals) {
            throw new IllegalArgumentException("currency mismatch: " + currency + " vs " + other.currency);
        }
    }
}
