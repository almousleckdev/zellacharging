package com.evcharge.customer.admin.application.port;

import java.time.Instant;

public interface AdminMailer {
    boolean sendInvitation(String email, String link, Instant expiresAt);
}
