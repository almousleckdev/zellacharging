package com.evcharge.customer.admin.application;

import com.evcharge.customer.account.CustomerDirectory;
import com.evcharge.customer.admin.application.port.AdminAuditLog;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.CustomerActivity;
import com.evcharge.customer.shared.LogFilter;
import com.evcharge.customer.shared.TenantContext;
import com.evcharge.customer.wallet.LedgerLog;
import com.evcharge.customer.wallet.LedgerPage;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class AdminCustomerHistoryService {

    private final AdminAccessService access;
    private final CustomerDirectory customers;
    private final LedgerLog ledger;
    private final CustomerActivity activity;
    private final AdminAuditLog audit;

    public AdminCustomerHistoryService(AdminAccessService access, CustomerDirectory customers, LedgerLog ledger,
                                       CustomerActivity activity, AdminAuditLog audit) {
        this.access = access;
        this.customers = customers;
        this.ledger = ledger;
        this.activity = activity;
        this.audit = audit;
    }

    public LedgerPage ledger(AdminAccess admin, String country, UUID id, LogFilter filter, int page, int size) {
        String code = AdminCountries.single(access.scope(admin, Permission.CUSTOMERS_READ, country));
        PageWindow window = PageWindow.of(page, size);
        return TenantContext.callAs(code, () -> {
            requireCustomer(id);
            return ledger.search(id, filter, window.size(), window.offset());
        });
    }

    public ActivityListing activity(AdminAccess admin, String country, UUID id, int page, int size) {
        String code = AdminCountries.single(access.scope(admin, Permission.CUSTOMERS_READ, country));
        PageWindow window = PageWindow.of(page, size);
        List<ActivityView> merged = new ArrayList<>();
        long customerEvents = TenantContext.callAs(code, () -> {
            requireCustomer(id);
            activity.latest(id, window.end()).forEach(entry -> merged.add(new ActivityView("CUSTOMER", null,
                    entry.action(), entry.detail(), entry.createdAt())));
            return activity.count(id);
        });
        var staff = audit.search(new AuditQuery(List.of(code), false, null, null, id.toString(), LogFilter.NONE,
                window.end(), 0));
        staff.items().forEach(entry -> merged.add(new ActivityView("ADMIN", entry.adminEmail(), entry.action(),
                entry.detail(), entry.createdAt())));
        merged.sort(Comparator.comparing(ActivityView::createdAt).reversed());
        int from = Math.min(window.offset(), merged.size());
        int to = Math.min(window.end(), merged.size());
        return new ActivityListing(customerEvents + staff.total(), window.index(), window.size(),
                List.copyOf(merged.subList(from, to)));
    }

    private void requireCustomer(UUID id) {
        customers.find(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND",
                "Customer not found"));
    }
}
