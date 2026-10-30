package com.evcharge.customer.notification;

import com.evcharge.customer.notification.domain.DeviceToken;
import com.evcharge.customer.notification.domain.Platform;
import com.evcharge.customer.notification.domain.PushContent;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FcmMessageTest {

    @SuppressWarnings("unchecked")
    private Map<String, Object> built(Platform platform) throws Exception {
        var factory = Class.forName("com.evcharge.customer.notification.infrastructure.FcmMessage");
        var method = factory.getDeclaredMethod("of", DeviceToken.class, PushContent.class, String.class);
        method.setAccessible(true);
        var device = new DeviceToken(UUID.randomUUID(), "tok", platform, "fr");
        return (Map<String, Object>) method.invoke(null, device, new PushContent("t", "b", Map.of(), 3), "zella_alerts");
    }

    @Test
    @SuppressWarnings("unchecked")
    void iosShowsOnALockedPhoneWithSoundAndTheBadgeCount() throws Exception {
        var apns = (Map<String, Object>) built(Platform.IOS).get("apns");
        var headers = (Map<String, String>) apns.get("headers");
        var aps = (Map<String, Object>) ((Map<String, Object>) apns.get("payload")).get("aps");

        assertThat(headers).containsEntry("apns-push-type", "alert").containsEntry("apns-priority", "10");
        assertThat(aps).containsEntry("sound", "default").containsEntry("badge", 3);
    }

    @Test
    @SuppressWarnings("unchecked")
    void androidUsesTheHighPriorityChannelShowsOnTheLockScreenAndSetsTheCount() throws Exception {
        var android = (Map<String, Object>) built(Platform.ANDROID).get("android");
        var notification = (Map<String, Object>) android.get("notification");

        assertThat(android).containsEntry("priority", "HIGH");
        assertThat(notification).containsEntry("channel_id", "zella_alerts").containsEntry("visibility", "PUBLIC")
                .containsEntry("notification_count", 3).containsEntry("default_sound", true);
    }
}
