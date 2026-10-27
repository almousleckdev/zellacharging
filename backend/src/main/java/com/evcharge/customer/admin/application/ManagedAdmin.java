package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.domain.AdminUser;

import java.time.Instant;

public record ManagedAdmin(AdminUser user, Instant avatarUpdatedAt) {
}
