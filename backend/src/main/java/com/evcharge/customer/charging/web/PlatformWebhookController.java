package com.evcharge.customer.charging.web;

import com.evcharge.customer.charging.application.PlatformEvents;
import com.evcharge.customer.charging.application.WebhookSettings;
import com.evcharge.customer.charging.application.WebhookSignature;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;

@RestController
@RequestMapping("/v1/ocpp")
class PlatformWebhookController {

    private final PlatformEvents events;
    private final PlatformEventParser parser;
    private final WebhookSettings settings;
    private final Clock clock;

    PlatformWebhookController(PlatformEvents events, PlatformEventParser parser, WebhookSettings settings,
                              Clock clock) {
        this.events = events;
        this.parser = parser;
        this.settings = settings;
        this.clock = clock;
    }

    @PostMapping("/webhooks")
    ResponseEntity<Void> receive(@RequestBody byte[] body,
                                 @RequestHeader(name = "X-Webhook-Signature", required = false) String signature,
                                 @RequestHeader(name = "X-Webhook-Signature-Timestamp", required = false)
                                 String timestamp) {
        if (!WebhookSignature.valid(body, signature, timestamp, settings.secret(), clock.instant(),
                settings.tolerance())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "WEBHOOK_SIGNATURE_INVALID",
                    "The webhook signature is not valid");
        }
        events.accept(parser.parse(body));
        return ResponseEntity.accepted().build();
    }
}
