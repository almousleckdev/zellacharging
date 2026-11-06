package com.evcharge.customer.partner.web;

import com.evcharge.customer.partner.application.PartnerPrincipal;
import com.evcharge.customer.partner.application.SessionFeedService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/partners/ndzela")
class SessionFeedController {

    private final SessionFeedService feed;

    SessionFeedController(SessionFeedService feed) {
        this.feed = feed;
    }

    @GetMapping("/sessions")
    FeedResponse<FeedItemView> sessions(@AuthenticationPrincipal PartnerPrincipal principal,
                          @RequestParam(required = false) String cursor,
                          @RequestParam(required = false) Integer limit,
                          @RequestParam(required = false) UUID customerId) {
        return FeedResponse.of(feed.read(principal, cursor, limit, customerId));
    }

    @GetMapping("/public-sessions")
    FeedResponse<PublicFeedItemView> publicSessions(@AuthenticationPrincipal PartnerPrincipal principal,
                                                    @RequestParam(required = false) String cursor,
                                                    @RequestParam(required = false) Integer limit) {
        return FeedResponse.ofPublic(feed.readPublic(principal, cursor, limit));
    }
}
