package com.evcharge.customer.admin.application;

import com.evcharge.customer.account.CustomerDirectory;
import com.evcharge.customer.account.CustomerPage;
import com.evcharge.customer.account.CustomerQuery;
import com.evcharge.customer.account.CustomerRecord;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.charging.SessionStatistics;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.TenantContext;
import com.evcharge.customer.wallet.WalletStatistics;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class AdminCustomerService {

    public static final int MAX_PAGE_SIZE = 100;
    private static final Comparator<CountryCustomer> NEWEST_FIRST = Comparator
            .comparing((CountryCustomer entry) -> entry.customer().createdAt()).reversed()
            .thenComparing(entry -> entry.customer().id());

    private final AdminAccessService access;
    private final CustomerDirectory customers;
    private final WalletStatistics wallets;
    private final SessionStatistics sessions;
    private final AdminAudit audit;

    public AdminCustomerService(AdminAccessService access, CustomerDirectory customers, WalletStatistics wallets,
                                SessionStatistics sessions, AdminAudit audit) {
        this.access = access;
        this.customers = customers;
        this.wallets = wallets;
        this.sessions = sessions;
        this.audit = audit;
    }

    public CustomerListing list(AdminAccess admin, String country, String text, String status, int page, int size) {
        List<String> scope = access.scope(admin, Permission.CUSTOMERS_READ, country);
        int pageSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        int pageIndex = Math.max(0, page);
        int offset = pageIndex * pageSize;
        CustomerQuery query = new CustomerQuery(blankToNull(text), blankToNull(status), offset + pageSize, 0);
        long total = 0;
        List<CountryCustomer> merged = new ArrayList<>();
        for (String code : scope) {
            CustomerPage found = TenantContext.callAs(code, () -> customers.search(query));
            total += found.total();
            found.items().forEach(item -> merged.add(new CountryCustomer(code, item)));
        }
        merged.sort(NEWEST_FIRST);
        int from = Math.min(offset, merged.size());
        int to = Math.min(offset + pageSize, merged.size());
        return new CustomerListing(total, pageIndex, pageSize, List.copyOf(merged.subList(from, to)));
    }

    public CustomerDetail detail(AdminAccess admin, String country, UUID id) {
        List<String> scope = access.scope(admin, Permission.CUSTOMERS_READ, country);
        if (scope.size() != 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "COUNTRY_REQUIRED", "A single country is required");
        }
        String code = scope.getFirst();
        CustomerDetail detail = TenantContext.callAs(code, () -> {
            CustomerRecord record = customers.find(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                    "CUSTOMER_NOT_FOUND", "Customer not found"));
            return new CustomerDetail(code, record, wallets.ofCustomer(id).orElse(null), sessions.ofCustomer(id));
        });
        audit.record(admin.id(), code, "CUSTOMER_VIEWED", id.toString(), null);
        return detail;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
