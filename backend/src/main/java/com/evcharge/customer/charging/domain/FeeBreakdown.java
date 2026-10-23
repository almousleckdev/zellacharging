package com.evcharge.customer.charging.domain;

public record FeeBreakdown(long electricMinor, long vatMinor, long serviceMinor) {

    public long totalMinor() {
        return electricMinor + vatMinor + serviceMinor;
    }
}
