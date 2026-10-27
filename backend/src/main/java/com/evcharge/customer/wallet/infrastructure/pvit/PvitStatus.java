package com.evcharge.customer.wallet.infrastructure.pvit;

enum PvitStatus {
    SUCCESS,
    FAILED,
    PENDING;

    static PvitStatus from(String value) {
        if (value == null) {
            return PENDING;
        }
        return switch (value.trim().toUpperCase(java.util.Locale.ROOT)) {
            case "SUCCESS", "SUCCESSFUL", "COMPLETED" -> SUCCESS;
            case "FAILED", "FAILURE", "CANCELLED", "REJECTED", "EXPIRED" -> FAILED;
            default -> PENDING;
        };
    }
}
