package com.evcharge.customer.partner.application;

import com.evcharge.customer.partner.application.port.EvidenceStore;
import com.evcharge.customer.partner.domain.EvidenceItem;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

@Service
public class EvidenceFeedService {

    private final EvidenceStore evidence;
    private final PartnerSettings settings;
    private final Clock clock;

    EvidenceFeedService(EvidenceStore evidence, PartnerSettings settings, Clock clock) {
        this.evidence = evidence;
        this.settings = settings;
        this.clock = clock;
    }

    public EvidencePage read(PartnerPrincipal principal, String cursor, Integer limit, UUID customerId) {
        int size = limit == null ? settings.defaultFeedLimit() : limit;
        if (size < 1 || size > settings.maxFeedLimit()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PARTNER_LIMIT_INVALID",
                    "The limit must be between 1 and " + settings.maxFeedLimit());
        }
        long from = EvidenceCursor.decode(cursor);
        List<EvidenceItem> found = evidence.after(principal.partner(), from, size + 1, customerId, clock.instant().minus(settings.feedLag()));
        boolean more = found.size() > size;
        List<EvidenceItem> items = more ? found.subList(0, size) : found;
        long last = items.isEmpty() ? from : items.get(items.size() - 1).sequence();
        return new EvidencePage(items, EvidenceCursor.encode(last), more);
    }
}
