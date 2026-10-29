package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.IncidentReviews;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.Incident;
import com.evcharge.customer.shared.IncidentFeed;
import com.evcharge.customer.shared.LogFilter;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AdminIncidentService {

    private static final int PER_FEED = 300;
    private static final Set<String> REVIEW_STATES = Set.of("ACKNOWLEDGED", "RESOLVED");

    private final AdminAccessService access;
    private final List<IncidentFeed> feeds;
    private final IncidentReviews reviews;
    private final AdminAudit audit;
    private final Clock clock;

    public AdminIncidentService(AdminAccessService access, List<IncidentFeed> feeds, IncidentReviews reviews,
                                AdminAudit audit, Clock clock) {
        this.access = access;
        this.feeds = feeds;
        this.reviews = reviews;
        this.audit = audit;
        this.clock = clock;
    }

    public IncidentListing list(AdminAccess admin, String country, String severity, String status, String kind,
                                LogFilter filter, int page, int size) {
        List<String> scope = access.scope(admin, Permission.OVERVIEW_READ, country);
        PageWindow window = PageWindow.of(page, size);
        List<IncidentView> everything = new ArrayList<>();
        scope.forEach(code -> everything.addAll(viewsOf(code, filter)));
        everything.sort(Comparator.comparing((IncidentView view) -> view.incident().occurredAt()).reversed());
        long open = everything.stream().filter(view -> "OPEN".equals(view.status())).count();
        long high = everything.stream().filter(view -> "OPEN".equals(view.status())
                && "HIGH".equals(view.incident().severity())).count();
        List<IncidentView> matching = everything.stream()
                .filter(view -> severity == null || severity.isBlank() || view.incident().severity().equals(severity))
                .filter(view -> status == null || status.isBlank() || view.status().equals(status))
                .filter(view -> kind == null || kind.isBlank() || view.incident().kind().equals(kind)).toList();
        int from = Math.min(window.offset(), matching.size());
        int to = Math.min(window.end(), matching.size());
        return new IncidentListing(matching.size(), open, high, window.index(), window.size(),
                List.copyOf(matching.subList(from, to)));
    }

    public void review(AdminAccess admin, String country, String key, String status, String note) {
        String code = AdminCountries.single(access.scope(admin, Permission.OVERVIEW_READ, country));
        if (!REVIEW_STATES.contains(status)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_INCIDENT_STATUS", "Invalid status");
        }
        reviews.save(code, key, status, note == null || note.isBlank() ? null : note.trim(), admin.id(),
                clock.instant());
        audit.record(admin.id(), code, "INCIDENT_" + status, key, note);
    }

    private List<IncidentView> viewsOf(String code, LogFilter filter) {
        Map<String, IncidentReview> reviewed = reviews.ofCountry(code);
        List<IncidentView> views = new ArrayList<>();
        for (IncidentFeed feed : feeds) {
            for (Incident incident : TenantContext.callAs(code, () -> feed.recent(filter, PER_FEED))) {
                IncidentReview review = reviewed.get(incident.key());
                views.add(new IncidentView(code, incident, review == null ? "OPEN" : review.status(),
                        review == null ? null : review.note()));
            }
        }
        return views;
    }
}
