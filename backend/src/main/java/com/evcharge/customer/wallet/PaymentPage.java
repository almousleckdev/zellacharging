package com.evcharge.customer.wallet;

import java.util.List;

public record PaymentPage(long total, List<PaymentRecord> items) {
}
