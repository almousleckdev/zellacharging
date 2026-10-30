package com.evcharge.customer.notification.web;

import com.evcharge.customer.notification.application.NotificationInboxService;
import com.evcharge.customer.shared.CustomerPrincipal;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/notifications")
class NotificationController {

    private final NotificationInboxService inbox;

    NotificationController(NotificationInboxService inbox) {
        this.inbox = inbox;
    }

    @GetMapping
    InboxResponse list(CustomerPrincipal principal, @RequestParam(defaultValue = "50") int limit) {
        String language = LocaleContextHolder.getLocale().getLanguage();
        return InboxResponse.from(inbox.list(principal.id(), language, limit));
    }

    @GetMapping("/unread-count")
    Map<String, Integer> unread(CustomerPrincipal principal) {
        return Map.of("count", inbox.unread(principal.id()));
    }

    @PostMapping("/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void readAll(CustomerPrincipal principal) {
        inbox.markAllRead(principal.id());
    }

    @PostMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void read(CustomerPrincipal principal, @PathVariable UUID id) {
        inbox.markRead(principal.id(), id);
    }
}
