package com.evcharge.customer.wallet;

public record PaymentDigest(String customerName, long operations, long topupsMinor, long withdrawalsMinor,
                            PaymentRecord last) {
}
