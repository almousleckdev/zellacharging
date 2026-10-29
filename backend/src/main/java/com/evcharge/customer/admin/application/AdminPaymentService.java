package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.wallet.PaymentDetail;
import com.evcharge.customer.wallet.PaymentLog;
import com.evcharge.customer.wallet.PaymentPage;
import com.evcharge.customer.wallet.PaymentQuery;
import com.evcharge.customer.wallet.PaymentRecord;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.LogFilter;
import com.evcharge.customer.shared.Sort;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AdminPaymentService {

    private final AdminAccessService access;
    private final PaymentLog payments;

    public AdminPaymentService(AdminAccessService access, PaymentLog payments) {
        this.access = access;
        this.payments = payments;
    }

    public CountryListing<PaymentRecord> list(AdminAccess admin, String country, String kind, String state,
                                              String text,
                                              LogFilter filter, String sortBy, int page, int size) {
        List<String> scope = access.scope(admin, Permission.PAYMENTS_READ, country);
        Sort sort = Sort.parse(sortBy, SortOrders.PAYMENT_FIELDS, "date");
        return CountryMerge.ordered(scope, PageWindow.of(page, size), limit -> {
            PaymentPage found = payments.search(new PaymentQuery(blank(kind), blank(state), blank(text), filter, sort,
                    limit));
            return new CountryMerge.Found<>(found.total(), found.items());
        }, PaymentRecord::createdAt, SortOrders.payments(sort));
    }

    public CountryEntry<PaymentDetail> detail(AdminAccess admin, String country, String kind, UUID id) {
        String code = AdminCountries.single(access.scope(admin, Permission.PAYMENTS_READ, country));
        PaymentDetail found = TenantContext.callAs(code, () -> payments.find(kind.toUpperCase(), id)).orElseThrow(
                () -> new ApiException(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", "Payment not found"));
        return new CountryEntry<>(code, found, found.record().createdAt());
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
