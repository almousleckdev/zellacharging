package com.evcharge.customer.admin.application;

import com.evcharge.customer.account.CustomerBreakdown;
import com.evcharge.customer.account.CustomerCounts;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.stereotype.Service;

@Service
public class AdminCustomerCountService {

    private final AdminAccessService access;
    private final CustomerBreakdown breakdown;

    public AdminCustomerCountService(AdminAccessService access, CustomerBreakdown breakdown) {
        this.access = access;
        this.breakdown = breakdown;
    }

    public CustomerCounts counts(AdminAccess admin, String country) {
        return access.scope(admin, Permission.CUSTOMERS_READ, country).stream()
                .map(code -> TenantContext.callAs(code, breakdown::counts))
                .reduce(new CustomerCounts(0, 0, 0, 0, 0), CustomerCounts::plus);
    }
}
