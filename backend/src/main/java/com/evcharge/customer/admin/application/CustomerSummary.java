package com.evcharge.customer.admin.application;

import com.evcharge.customer.charging.CustomerSessions;
import com.evcharge.customer.wallet.CustomerWallet;

public record CustomerSummary(CustomerWallet wallet, CustomerSessions sessions, boolean ndzelaDriver) {
}
