package com.evcharge.customer.admin.application;

import com.evcharge.customer.account.Complimentary;
import com.evcharge.customer.account.CustomerRecord;
import com.evcharge.customer.charging.CustomerSessions;
import com.evcharge.customer.wallet.CustomerWallet;

public record CustomerDetail(String country, CustomerRecord customer, CustomerWallet wallet,
                             CustomerSessions sessions, Complimentary complimentary) {
}
