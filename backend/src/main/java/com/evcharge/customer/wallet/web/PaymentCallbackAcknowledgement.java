package com.evcharge.customer.wallet.web;

record PaymentCallbackAcknowledgement(Object transactionId, Object responseCode) {

    static PaymentCallbackAcknowledgement echoing(PaymentCallbackRequest request) {
        return new PaymentCallbackAcknowledgement(request.transactionId(), request.code());
    }
}
