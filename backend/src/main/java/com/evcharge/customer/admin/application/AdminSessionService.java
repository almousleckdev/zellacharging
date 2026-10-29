package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.charging.SessionDetail;
import com.evcharge.customer.charging.SessionLog;
import com.evcharge.customer.charging.SessionLogQuery;
import com.evcharge.customer.charging.SessionPage;
import com.evcharge.customer.charging.SessionRecord;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.LogFilter;
import com.evcharge.customer.shared.Sort;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AdminSessionService {

    private final AdminAccessService access;
    private final SessionLog sessions;

    public AdminSessionService(AdminAccessService access, SessionLog sessions) {
        this.access = access;
        this.sessions = sessions;
    }

    public CountryListing<SessionRecord> list(AdminAccess admin, String country, String state, String text,
                                              LogFilter filter, String sortBy, int page, int size) {
        List<String> scope = access.scope(admin, Permission.SESSIONS_READ, country);
        Sort sort = Sort.parse(sortBy, SortOrders.SESSION_FIELDS, "date");
        return CountryMerge.ordered(scope, PageWindow.of(page, size), limit -> {
            SessionPage found = sessions.search(new SessionLogQuery(blank(state), blank(text), filter, sort, limit));
            return new CountryMerge.Found<>(found.total(), found.items());
        }, SessionRecord::createdAt, SortOrders.sessions(sort));
    }

    public CountryEntry<SessionDetail> detail(AdminAccess admin, String country, UUID id) {
        String code = AdminCountries.single(access.scope(admin, Permission.SESSIONS_READ, country));
        SessionDetail found = TenantContext.callAs(code, () -> sessions.find(id)).orElseThrow(
                () -> new ApiException(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", "Session not found"));
        return new CountryEntry<>(code, found, found.record().createdAt());
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
