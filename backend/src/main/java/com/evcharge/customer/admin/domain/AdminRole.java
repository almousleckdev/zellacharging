package com.evcharge.customer.admin.domain;

import java.util.EnumSet;
import java.util.Set;

import static com.evcharge.customer.admin.domain.Permission.AUDIT_READ;
import static com.evcharge.customer.admin.domain.Permission.CUSTOMERS_MANAGE;
import static com.evcharge.customer.admin.domain.Permission.CUSTOMERS_READ;
import static com.evcharge.customer.admin.domain.Permission.OVERVIEW_READ;
import static com.evcharge.customer.admin.domain.Permission.PAYMENTS_READ;
import static com.evcharge.customer.admin.domain.Permission.SESSIONS_READ;
import static com.evcharge.customer.admin.domain.Permission.SESSIONS_STOP;
import static com.evcharge.customer.admin.domain.Permission.STATIONS_READ;
import static com.evcharge.customer.admin.domain.Permission.WITHDRAWALS_REVIEW;

public enum AdminRole {
    SUPER_ADMIN(EnumSet.allOf(Permission.class)),
    ADMIN(EnumSet.of(OVERVIEW_READ, STATIONS_READ, SESSIONS_READ, SESSIONS_STOP, CUSTOMERS_READ, CUSTOMERS_MANAGE,
            PAYMENTS_READ, WITHDRAWALS_REVIEW, AUDIT_READ)),
    SUPPORT(EnumSet.of(OVERVIEW_READ, CUSTOMERS_READ, SESSIONS_READ)),
    FINANCE(EnumSet.of(OVERVIEW_READ, CUSTOMERS_READ, SESSIONS_READ, PAYMENTS_READ)),
    OPERATIONS(EnumSet.of(OVERVIEW_READ, STATIONS_READ, SESSIONS_READ, SESSIONS_STOP));

    private final Set<Permission> preset;

    AdminRole(Set<Permission> preset) {
        this.preset = preset;
    }

    public Set<Permission> permissions() {
        return preset;
    }

    public boolean spansAllCountries() {
        return this == SUPER_ADMIN;
    }
}
