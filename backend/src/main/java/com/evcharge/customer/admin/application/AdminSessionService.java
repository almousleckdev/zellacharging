package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.charging.SessionLog;
import com.evcharge.customer.charging.SessionLogQuery;
import com.evcharge.customer.charging.SessionPage;
import com.evcharge.customer.charging.SessionRecord;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminSessionService {

    private final AdminAccessService access;
    private final SessionLog sessions;

    public AdminSessionService(AdminAccessService access, SessionLog sessions) {
        this.access = access;
        this.sessions = sessions;
    }

    public CountryListing<SessionRecord> list(AdminAccess admin, String country, String state, String text, int page,
                                              int size) {
        List<String> scope = access.scope(admin, Permission.SESSIONS_READ, country);
        return CountryMerge.newestFirst(scope, PageWindow.of(page, size), limit -> {
            SessionPage found = sessions.search(new SessionLogQuery(blank(state), blank(text), limit));
            return new CountryMerge.Found<>(found.total(), found.items());
        }, SessionRecord::createdAt);
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
