package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.domain.AdminAccess;

final class FreeChargingActor {

    private static final int MAXIMUM_LENGTH = 100;

    private FreeChargingActor() {
    }

    static String of(AdminAccess admin) {
        String printable = admin.email() == null ? "" : admin.email().replaceAll("[^\\x21-\\x7E]", "");
        String label = printable.isEmpty() ? admin.id().toString() : printable;
        return label.length() <= MAXIMUM_LENGTH ? label : label.substring(0, MAXIMUM_LENGTH);
    }
}
