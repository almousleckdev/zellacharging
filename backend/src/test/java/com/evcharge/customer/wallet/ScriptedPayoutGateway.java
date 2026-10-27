package com.evcharge.customer.wallet;

import com.evcharge.customer.wallet.application.port.PayoutGateway;
import com.evcharge.customer.wallet.domain.MobileOperator;
import com.evcharge.customer.wallet.domain.PayoutOutcome;
import com.evcharge.customer.wallet.domain.PayoutRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ScriptedPayoutGateway implements PayoutGateway {

    final List<PayoutRequest> sent = new ArrayList<>();
    private final Map<String, PayoutOutcome> statuses = new ConcurrentHashMap<>();
    private PayoutOutcome answer = PayoutOutcome.pending("p-1");
    private RuntimeException failure;

    void answerWith(PayoutOutcome outcome) {
        this.answer = outcome;
    }

    void failWith(RuntimeException exception) {
        this.failure = exception;
    }

    void reportStatus(String reference, PayoutOutcome outcome) {
        statuses.put(reference, outcome);
    }

    void reset() {
        sent.clear();
        statuses.clear();
        answer = PayoutOutcome.pending("p-1");
        failure = null;
    }

    @Override
    public boolean supports(String country, MobileOperator operator) {
        return true;
    }

    @Override
    public PayoutOutcome payout(PayoutRequest request) {
        sent.add(request);
        if (failure != null) {
            throw failure;
        }
        return answer;
    }

    @Override
    public PayoutOutcome status(String reference) {
        return statuses.getOrDefault(reference, PayoutOutcome.pending(null));
    }
}
