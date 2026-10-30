package com.evcharge.customer.notification.infrastructure;

import com.evcharge.customer.notification.application.NotificationSettings;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({NotificationSettings.class, FcmSettings.class})
class NotificationConfiguration {
}
