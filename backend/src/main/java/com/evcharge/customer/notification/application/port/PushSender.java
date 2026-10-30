package com.evcharge.customer.notification.application.port;

import com.evcharge.customer.notification.domain.DeviceToken;
import com.evcharge.customer.notification.domain.PushContent;
import com.evcharge.customer.notification.domain.PushResult;

public interface PushSender {

    boolean available();

    PushResult send(DeviceToken device, PushContent content);
}
