package com.evcharge.customer.partner.web;

import com.evcharge.customer.partner.application.EvidenceFeedService;
import com.evcharge.customer.partner.application.EvidencePage;
import com.evcharge.customer.partner.application.PartnerPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/partners/ndzela/evidence")
class EvidenceController {

    private final EvidenceFeedService feed;

    EvidenceController(EvidenceFeedService feed) {
        this.feed = feed;
    }

    @GetMapping
    FeedResponse<EvidenceItemView> evidence(@AuthenticationPrincipal PartnerPrincipal principal,
                                            @RequestParam(required = false) String cursor,
                                            @RequestParam(required = false) Integer limit,
                                            @RequestParam(required = false) UUID customerId) {
        EvidencePage page = feed.read(principal, cursor, limit, customerId);
        return new FeedResponse<>(page.items().stream().map(EvidenceItemView::of).toList(), page.nextCursor(),
                page.hasMore());
    }
}
