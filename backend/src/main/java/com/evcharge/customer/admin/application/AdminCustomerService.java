package com.evcharge.customer.admin.application;

import com.evcharge.customer.account.ComplimentaryAccess;
import com.evcharge.customer.account.CustomerDirectory;
import com.evcharge.customer.account.CustomerModeration;
import com.evcharge.customer.account.CustomerPage;
import com.evcharge.customer.account.CustomerPicture;
import com.evcharge.customer.account.CustomerQuery;
import com.evcharge.customer.account.CustomerRecord;
import com.evcharge.customer.account.CustomerSupport;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.charging.CustomerIdTags;
import com.evcharge.customer.charging.SessionStatistics;
import com.evcharge.customer.partner.PartnerLinking;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.Sort;
import com.evcharge.customer.shared.TenantContext;
import com.evcharge.customer.wallet.WalletStatistics;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Service
public class AdminCustomerService {

    private final AdminAccessService access;
    private final CustomerDirectory customers;
    private final WalletStatistics wallets;
    private final SessionStatistics sessions;
    private final CustomerModeration moderation;
    private final ComplimentaryAccess complimentary;
    private final CustomerSupport support;
    private final PartnerLinking linking;
    private final CustomerIdTags idTags;
    private final AdminAudit audit;

    public AdminCustomerService(AdminAccessService access, CustomerDirectory customers, WalletStatistics wallets,
                                SessionStatistics sessions, CustomerModeration moderation,
                                ComplimentaryAccess complimentary, CustomerSupport support, PartnerLinking linking,
                                CustomerIdTags idTags, AdminAudit audit) {
        this.access = access;
        this.customers = customers;
        this.wallets = wallets;
        this.sessions = sessions;
        this.moderation = moderation;
        this.complimentary = complimentary;
        this.support = support;
        this.linking = linking;
        this.idTags = idTags;
        this.audit = audit;
    }

    public CustomerListing list(AdminAccess admin, String country, String text, String status, String sortBy,
                                int page, int size) {
        List<String> scope = access.scope(admin, Permission.CUSTOMERS_READ, country);
        PageWindow window = PageWindow.of(page, size);
        Sort sort = Sort.parse(sortBy, SortOrders.CUSTOMER_FIELDS, "date");
        CustomerQuery query = new CustomerQuery(blankToNull(text), blankToNull(status), sort, window.end(), 0);
        CountryListing<CustomerRecord> found = CountryMerge.ordered(scope, window, limit -> {
            CustomerPage result = customers.search(new CustomerQuery(query.text(), query.status(), sort, limit, 0));
            return new CountryMerge.Found<>(result.total(), result.items());
        }, CustomerRecord::createdAt, SortOrders.customers(sort));
        List<CountryCustomer> items = new ArrayList<>();
        for (String code : scope) {
            List<CustomerRecord> mine = found.items().stream().filter(entry -> entry.country().equals(code))
                    .map(CountryEntry::item).toList();
            items.addAll(summarised(code, mine));
        }
        items.sort(Comparator.comparing(CountryCustomer::customer, SortOrders.customers(sort)));
        return new CustomerListing(found.total(), found.page(), found.size(), items);
    }

    private List<CountryCustomer> summarised(String code, List<CustomerRecord> records) {
        if (records.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = records.stream().map(CustomerRecord::id).toList();
        return TenantContext.callAs(code, () -> {
            var wallets = this.wallets.ofCustomers(ids);
            var activity = sessions.ofCustomers(ids);
            return records.stream().map(record -> new CountryCustomer(code, record,
                    new CustomerSummary(wallets.get(record.id()), activity.get(record.id())))).toList();
        });
    }

    public CustomerDetail detail(AdminAccess admin, String country, UUID id) {
        List<String> scope = access.scope(admin, Permission.CUSTOMERS_READ, country);
        String code = AdminCountries.single(scope);
        return TenantContext.callAs(code, () -> {
            CustomerRecord record = customers.find(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                    "CUSTOMER_NOT_FOUND", "Customer not found"));
            return new CustomerDetail(code, record, wallets.ofCustomer(id).orElse(null), sessions.ofCustomer(id),
                    complimentary.find(id).orElse(null), support.state(id),
                    new NdzelaLink(linking.linked(id), idTags.existing(List.of(id)).get(id)));
        });
    }

    public Optional<CustomerPicture> picture(AdminAccess admin, String country, UUID id) {
        String code = AdminCountries.single(access.scope(admin, Permission.CUSTOMERS_READ, country));
        return TenantContext.callAs(code, () -> customers.picture(id));
    }

    public void suspend(AdminAccess admin, String country, UUID id) {
        moderate(admin, country, id, "CUSTOMER_SUSPENDED", moderation::suspend);
    }

    public void reactivate(AdminAccess admin, String country, UUID id) {
        moderate(admin, country, id, "CUSTOMER_REACTIVATED", moderation::reactivate);
    }

    public void delete(AdminAccess admin, String country, UUID id) {
        moderate(admin, country, id, "CUSTOMER_DELETED", moderation::delete);
    }

    private void moderate(AdminAccess admin, String country, UUID id, String action, Consumer<UUID> change) {
        List<String> scope = access.scope(admin, Permission.CUSTOMERS_MANAGE, country);
        String code = AdminCountries.single(scope);
        TenantContext.runAs(code, () -> change.accept(id));
        audit.record(admin.id(), code, action, id.toString(), null);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
