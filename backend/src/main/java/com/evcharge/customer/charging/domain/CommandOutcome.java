package com.evcharge.customer.charging.domain;

public record CommandOutcome(boolean accepted, String reasonCode, String message) {

    public static CommandOutcome success() {
        return new CommandOutcome(true, null, null);
    }

    public static CommandOutcome refused(String reasonCode, String message) {
        return new CommandOutcome(false, reasonCode, message);
    }
}
