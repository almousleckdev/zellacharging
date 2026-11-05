package com.evcharge.customer.partner.application;

import com.evcharge.customer.charging.ChargeFeed;
import com.evcharge.customer.charging.ChargeFeedItem;
import com.evcharge.customer.charging.ChargeFeedPage;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.AuditTrail;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

@Service
public class SessionFeedService {

    private final ChargeFeed feed;
    private final LinkService links;
    private final PartnerSettings settings;
    private final AuditTrail audit;
    private final Clock clock;

    SessionFeedService(ChargeFeed feed, LinkService links, PartnerSettings settings, AuditTrail audit,
                       Clock clock) {
        this.feed = feed;
        this.links = links;
        this.settings = settings;
        this.audit = audit;
        this.clock = clock;
    }

    public FeedPage read(PartnerPrincipal principal, String cursor, Integer limit, UUID customerId) {
        int size = limit == null ? settings.defaultFeedLimit() : limit;
        if (size < 1 || size > settings.maxFeedLimit()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PARTNER_LIMIT_INVALID",
                    "The limit must be between 1 and " + settings.maxFeedLimit());
        }
        FeedCursor from = cursor == null || cursor.isBlank() ? FeedCursor.START : FeedCursor.decode(cursor);
        ChargeFeedPage page = feed.after(from.updatedAt(), from.sessionId(), size, customers(principal, customerId),
                clock.instant().minus(settings.feedLag()));
        List<ChargeFeedItem> items = page.items();
        if (items.isEmpty()) {
            return new FeedPage(items, cursor == null || cursor.isBlank() ? null : cursor, false);
        }
        ChargeFeedItem last = items.get(items.size() - 1);
        audit.record("PARTNER_FEED_READ", principal.keyId(), principal.partner(), "items=" + items.size());
        return new FeedPage(items, FeedCursor.encode(last.updatedAt(), last.sessionId()), page.hasMore());
    }

    private List<UUID> customers(PartnerPrincipal principal, UUID customerId) {
        List<UUID> linked = links.linkedCustomerIds(principal);
        if (customerId == null) {
            return linked;
        }
        if (!linked.contains(customerId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Customer not linked");
        }
        return List.of(customerId);
    }
}
