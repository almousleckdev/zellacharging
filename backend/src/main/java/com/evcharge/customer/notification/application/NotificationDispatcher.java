package com.evcharge.customer.notification.application;

import com.evcharge.customer.notification.application.port.DeviceTokenRepository;
import com.evcharge.customer.notification.application.port.NotificationInbox;
import com.evcharge.customer.notification.application.port.NotificationOutbox;
import com.evcharge.customer.notification.application.port.PushSender;
import com.evcharge.customer.notification.domain.DeviceToken;
import com.evcharge.customer.notification.domain.PendingNotification;
import com.evcharge.customer.notification.domain.PushResult;
import com.evcharge.customer.shared.Country;
import com.evcharge.customer.shared.CountryDirectory;
import com.evcharge.customer.shared.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final NotificationOutbox outbox;
    private final NotificationInbox inbox;
    private final DeviceTokenRepository devices;
    private final PushSender sender;
    private final NotificationTexts texts;
    private final NotificationSettings settings;
    private final CountryDirectory countries;
    private final Clock clock;

    public NotificationDispatcher(NotificationOutbox outbox, NotificationInbox inbox, DeviceTokenRepository devices,
                                  PushSender sender, NotificationTexts texts, NotificationSettings settings,
                                  CountryDirectory countries, Clock clock) {
        this.outbox = outbox;
        this.inbox = inbox;
        this.devices = devices;
        this.sender = sender;
        this.texts = texts;
        this.settings = settings;
        this.countries = countries;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${zella.notifications.dispatch-interval}",
            initialDelayString = "${zella.notifications.dispatch-interval}")
    public void run() {
        if (!sender.available()) {
            return;
        }
        for (Country country : countries.active()) {
            TenantContext.runAs(country.code(), this::dispatchDue);
        }
    }

    void dispatchDue() {
        Instant now = clock.instant();
        outbox.purgeFinishedBefore(now.minus(settings.retention()));
        for (PendingNotification notification : outbox.claimDue(now, settings.batchSize())) {
            try {
                deliver(notification, now);
            } catch (RuntimeException e) {
                log.error("Notification {} could not be processed", notification.id(), e);
                retryOrBury(notification, now);
            }
        }
    }

    private void deliver(PendingNotification notification, Instant now) {
        List<DeviceToken> targets = devices.forCustomer(notification.customerId());
        int delivered = 0;
        boolean retry = false;
        int unread = targets.isEmpty() ? 0 : inbox.unread(notification.customerId());
        for (DeviceToken device : targets) {
            PushResult result = sender.send(device, texts.render(notification, device.language()).withBadge(unread));
            switch (result) {
                case DELIVERED -> delivered++;
                case INVALID_TOKEN -> devices.removeToken(device.token());
                case RETRY -> retry = true;
            }
        }
        report(notification, targets, delivered);
        if (retry && delivered == 0) {
            retryOrBury(notification, now);
        } else {
            outbox.markSent(notification.id(), now, delivered);
        }
    }

    private void report(PendingNotification notification, List<DeviceToken> devices, int delivered) {
        if (devices.isEmpty()) {
            log.warn("Push {} for customer {} not sent: no device is registered", notification.type(),
                    notification.customerId());
            return;
        }
        log.info("Push {} for customer {}: accepted by Firebase for {} of {} devices ({})", notification.type(),
                notification.customerId(), delivered, devices.size(),
                devices.stream().map(device -> device.platform().name()).collect(Collectors.joining(", ")));
    }

    private void retryOrBury(PendingNotification notification, Instant now) {
        if (notification.attempts() + 1 >= settings.maxAttempts()) {
            outbox.markDead(notification.id(), now);
            log.error("ALERT notification {} gave up after {} attempts", notification.id(), settings.maxAttempts());
            return;
        }
        long factor = 1L << notification.attempts();
        outbox.markRetry(notification.id(), now.plus(settings.retryBase().multipliedBy(factor)));
    }
}
