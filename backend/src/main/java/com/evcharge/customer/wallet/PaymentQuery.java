package com.evcharge.customer.wallet;

public record PaymentQuery(String kind, String state, String text, int limit) {
}
