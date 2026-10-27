package com.evcharge.customer.wallet;

import com.evcharge.customer.wallet.application.port.PaymentGateway;
import com.evcharge.customer.wallet.domain.CollectionRequest;
import com.evcharge.customer.wallet.domain.MobileOperator;
import com.evcharge.customer.wallet.domain.PaymentOutcome;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ScriptedPaymentGateway implements PaymentGateway {

    final List<CollectionRequest> collected = new ArrayList<>();
    private final Map<String, PaymentOutcome> statuses = new ConcurrentHashMap<>();
    private RuntimeException collectFailure;

    void failCollectionWith(RuntimeException failure) {
        this.collectFailure = failure;
    }

    void reportStatus(String reference, PaymentOutcome outcome) {
        statuses.put(reference, outcome);
    }

    void reset() {
        collected.clear();
        statuses.clear();
        collectFailure = null;
    }

    @Override
    public boolean supports(String country, MobileOperator operator) {
        return true;
    }

    @Override
    public PaymentOutcome collect(CollectionRequest request) {
        collected.add(request);
        if (collectFailure != null) {
            throw collectFailure;
        }
        return PaymentOutcome.pending(null);
    }

    @Override
    public PaymentOutcome status(String reference) {
        return statuses.getOrDefault(reference, PaymentOutcome.pending(null));
    }
}
