package com.evcharge.customer.account.application;

import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.SuspensionNotice;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
class SuspendedRefusal {

    private final SuspensionNotice notice;

    SuspendedRefusal(SuspensionNotice notice) {
        this.notice = notice;
    }

    ApiException of(UUID customerId) {
        Map<String, Object> details = notice.owedMinor(customerId)
                .stream().mapToObj(owed -> Map.<String, Object>of("owedMinor", owed)).findFirst().orElse(Map.of());
        return new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_SUSPENDED", "Account is suspended", details);
    }
}
