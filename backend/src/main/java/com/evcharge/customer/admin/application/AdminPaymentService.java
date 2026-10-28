package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.wallet.PaymentLog;
import com.evcharge.customer.wallet.PaymentPage;
import com.evcharge.customer.wallet.PaymentQuery;
import com.evcharge.customer.wallet.PaymentRecord;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminPaymentService {

    private final AdminAccessService access;
    private final PaymentLog payments;

    public AdminPaymentService(AdminAccessService access, PaymentLog payments) {
        this.access = access;
        this.payments = payments;
    }

    public CountryListing<PaymentRecord> list(AdminAccess admin, String country, String kind, String state,
                                              String text, int page, int size) {
        List<String> scope = access.scope(admin, Permission.PAYMENTS_READ, country);
        return CountryMerge.newestFirst(scope, PageWindow.of(page, size), limit -> {
            PaymentPage found = payments.search(new PaymentQuery(blank(kind), blank(state), blank(text), limit));
            return new CountryMerge.Found<>(found.total(), found.items());
        }, PaymentRecord::createdAt);
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
