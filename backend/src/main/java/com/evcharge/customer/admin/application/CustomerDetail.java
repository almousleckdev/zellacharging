package com.evcharge.customer.admin.application;

import com.evcharge.customer.account.Complimentary;
import com.evcharge.customer.account.CustomerRecord;
import com.evcharge.customer.account.CustomerSupportState;
import com.evcharge.customer.account.SignInMethod;
import com.evcharge.customer.charging.CustomerSessions;
import com.evcharge.customer.wallet.CustomerWallet;

import java.util.List;

public record CustomerDetail(String country, CustomerRecord customer, CustomerWallet wallet,
                             CustomerSessions sessions, Complimentary complimentary, CustomerSupportState support,
                             NdzelaLink ndzela, List<SignInMethod> signInMethods) {
}
