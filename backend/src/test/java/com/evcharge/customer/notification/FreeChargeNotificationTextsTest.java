package com.evcharge.customer.notification;

import com.evcharge.customer.notification.application.NotificationTexts;
import com.evcharge.customer.notification.domain.PushContent;
import com.evcharge.customer.shared.NotificationType;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FreeChargeNotificationTextsTest {

    private static final NotificationType[] FREE = {NotificationType.FREE_CHARGE_STARTED,
            NotificationType.FREE_WINDOW_ENDING, NotificationType.FREE_WINDOW_ENDED};

    private final NotificationTexts texts = new NotificationTexts();

    private PushContent render(NotificationType type, Map<String, String> params, String language) {
        return texts.render(UUID.randomUUID(), type, params, language);
    }

    @Test
    void everyFreeChargeNoticeExistsInFrenchAndEnglishAndTheTwoDiffer() {
        for (NotificationType type : FREE) {
            PushContent french = render(type, Map.of("time", "23:59"), "fr");
            PushContent english = render(type, Map.of("time", "23:59"), "en");

            assertThat(french.title()).isNotBlank();
            assertThat(french.body()).isNotBlank();
            assertThat(english.title()).isNotBlank();
            assertThat(english.body()).isNotBlank();
            assertThat(french.body()).isNotEqualTo(english.body());
        }
    }

    @Test
    void theEndTimeIsWrittenIntoTheText() {
        for (NotificationType type : FREE) {
            assertThat(render(type, Map.of("time", "22:00"), "fr").body()).contains("22:00").doesNotContain("{time}");
            assertThat(render(type, Map.of("time", "22:00"), "en").body()).contains("22:00").doesNotContain("{time}");
        }
    }

    @Test
    void aMissingTimeNeverLeavesAPlaceholderInTheText() {
        for (NotificationType type : FREE) {
            assertThat(render(type, Map.of(), "fr").body()).doesNotContain("{time}").isNotBlank();
            assertThat(render(type, Map.of(), "en").body()).doesNotContain("{time}").isNotBlank();
        }
    }

    @Test
    void theTimeIsNeverAddedToOtherNotices() {
        PushContent content = render(NotificationType.CHARGE_STARTED, Map.of("time", "22:00"), "en");

        assertThat(content.body()).isEqualTo("Your vehicle is charging.");
    }

    @Test
    void theNoticeCarriesItsTypeForTheAppToOpenTheRightScreen() {
        PushContent content = render(NotificationType.FREE_WINDOW_ENDING, Map.of("screen", "charging"), "fr");

        assertThat(content.data()).containsEntry("type", "FREE_WINDOW_ENDING").containsEntry("screen", "charging");
    }

    @Test
    void withoutATimeTheSentencesStillReadWell() {
        assertThat(render(NotificationType.FREE_CHARGE_STARTED, Map.of(), "fr").body()).isEqualTo("Cette charge est gratuite. Elle s'arrête.");
        assertThat(render(NotificationType.FREE_WINDOW_ENDING, Map.of(), "fr").body()).isEqualTo("La charge gratuite s'arrête bientôt.");
        assertThat(render(NotificationType.FREE_WINDOW_ENDING, Map.of(), "en").body()).isEqualTo("The free charge stops soon.");
        assertThat(render(NotificationType.FREE_WINDOW_ENDED, Map.of(), "en").body()).isEqualTo("Your free charge stopped.");
    }
}
