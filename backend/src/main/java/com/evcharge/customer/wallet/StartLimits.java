package com.evcharge.customer.wallet;

public record StartLimits(long minimumMinor, long maximumMinor, String currency, int decimals) {
}
