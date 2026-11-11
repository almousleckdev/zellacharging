package com.evcharge.customer.notification;

import com.evcharge.customer.notification.application.NotificationTexts;
import com.evcharge.customer.notification.domain.PushContent;
import com.evcharge.customer.shared.NotificationType;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SuspensionNotificationTextsTest {

    private final NotificationTexts texts = new NotificationTexts();

    private PushContent render(String type, String language) {
        return texts.render(UUID.randomUUID(), NotificationType.valueOf(type), Map.of(), language);
    }

    @Test
    void everySuspensionNoticeExistsInFrenchAndEnglish() {
        for (String type : new String[] {"ACCOUNT_SUSPENDED_ARREARS", "ACCOUNT_RESTORED", "SUSPENDED_HELP_DETECTED",
                "HELPER_WARNED"}) {
            PushContent french = render(type, "fr");
            PushContent english = render(type, "en");

            assertThat(french.title()).isNotBlank();
            assertThat(french.body()).isNotBlank();
            assertThat(english.title()).isNotBlank();
            assertThat(english.body()).isNotBlank();
            assertThat(french.body()).isNotEqualTo(english.body());
        }
    }

    @Test
    void theHelperWarningNeverNamesAnyone() {
        for (String language : new String[] {"fr", "en"}) {
            String body = render("HELPER_WARNED", language).body();

            assertThat(body).doesNotContain("{").doesNotContain("@").doesNotContainPattern("\\d{3}");
        }
    }
}
