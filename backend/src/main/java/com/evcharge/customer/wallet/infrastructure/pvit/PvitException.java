package com.evcharge.customer.wallet.infrastructure.pvit;

class PvitException extends RuntimeException {

    private final boolean refused;
    private final boolean notFound;
    private final String failureCode;

    PvitException(String message, boolean refused) {
        this(message, refused, false, null);
    }

    PvitException(String message, boolean refused, boolean notFound, String failureCode) {
        super(message);
        this.refused = refused;
        this.notFound = notFound;
        this.failureCode = failureCode;
    }

    PvitException(String message, Throwable cause) {
        super(message, cause);
        this.refused = false;
        this.notFound = false;
        this.failureCode = null;
    }

    String failureCode() {
        return failureCode;
    }

    boolean refused() {
        return refused;
    }

    boolean notFound() {
        return notFound;
    }
}
