package com.evcharge.customer.wallet.infrastructure.pvit;

class PvitException extends RuntimeException {

    private final boolean refused;
    private final boolean notFound;

    PvitException(String message, boolean refused) {
        this(message, refused, false);
    }

    PvitException(String message, boolean refused, boolean notFound) {
        super(message);
        this.refused = refused;
        this.notFound = notFound;
    }

    PvitException(String message, Throwable cause) {
        super(message, cause);
        this.refused = false;
        this.notFound = false;
    }

    boolean refused() {
        return refused;
    }

    boolean notFound() {
        return notFound;
    }
}
