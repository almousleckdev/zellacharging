package com.evcharge.customer.wallet.domain;

public record CollectionRequest(String reference, long amountMinor, String currency, MobileOperator operator,
                                String msisdn) {
}
