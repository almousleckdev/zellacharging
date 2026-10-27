package com.evcharge.customer.wallet.domain;

public record PayoutRequest(String reference, long amountMinor, String currency, MobileOperator operator,
                            String msisdn) {
}
