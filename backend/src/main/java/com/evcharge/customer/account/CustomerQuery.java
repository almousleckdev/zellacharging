package com.evcharge.customer.account;

public record CustomerQuery(String text, String status, int limit, int offset) {
}
