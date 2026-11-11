package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.shared.SuspensionEvidenceFeed;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AdminEvidenceService {

    private static final int PER_COUNTRY = 300;

    private final AdminAccessService access;
    private final SuspensionEvidenceFeed feed;

    public AdminEvidenceService(AdminAccessService access, SuspensionEvidenceFeed feed) {
        this.access = access;
        this.feed = feed;
    }

    public EvidenceListing list(AdminAccess admin, String country, int page, int size) {
        List<String> scope = access.scope(admin, Permission.CUSTOMERS_MANAGE, country);
        PageWindow window = PageWindow.of(page, size);
        List<AdminEvidence> everything = new ArrayList<>();
        for (String code : scope) {
            TenantContext.callAs(code, () -> feed.recent(PER_COUNTRY))
                    .forEach(row -> everything.add(new AdminEvidence(code, row)));
        }
        everything.sort(Comparator.comparing((AdminEvidence item) -> item.row().occurredAt()).reversed());
        int from = Math.min(window.offset(), everything.size());
        int to = Math.min(window.end(), everything.size());
        return new EvidenceListing(everything.size(), window.index(), window.size(),
                List.copyOf(everything.subList(from, to)));
    }
}
