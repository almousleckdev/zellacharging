package com.evcharge.customer.wallet.infrastructure.pvit;

import com.evcharge.customer.wallet.application.port.PayoutGateway;
import com.evcharge.customer.wallet.domain.MobileOperator;
import com.evcharge.customer.wallet.domain.PayoutOutcome;
import com.evcharge.customer.wallet.domain.PayoutRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

class PvitPayoutGateway implements PayoutGateway {

    private static final Logger log = LoggerFactory.getLogger(PvitPayoutGateway.class);
    private static final String DESCRIPTION = "Zella Charging";

    private final MobileOperator operator;
    private final PvitApi api;

    PvitPayoutGateway(MobileOperator operator, PvitApi api) {
        this.operator = operator;
        this.api = api;
    }

    @Override
    public boolean supports(String country, MobileOperator operator) {
        return this.operator == operator && PvitOperators.codeOf(country, operator).isPresent();
    }

    @Override
    public PayoutOutcome payout(PayoutRequest request) {
        String country = request.reference().substring(1, 3);
        String operatorCode = PvitOperators.codeOf(country, request.operator()).orElseThrow();
        try {
            PvitStatus status = api.startPayout(request.reference(), BigDecimal.valueOf(request.amountMinor()),
                    operatorCode, PvitPhone.nationalFormat(request.msisdn()), DESCRIPTION);
            return status == PvitStatus.FAILED ? PayoutOutcome.failed("PROVIDER_REFUSED") : PayoutOutcome.pending(null);
        } catch (PvitException e) {
            if (e.refused()) {
                log.warn("PVit refused payout {}: {}", request.reference(), e.getMessage());
                return PayoutOutcome.failed("PROVIDER_REFUSED");
            }
            throw e;
        }
    }

    @Override
    public PayoutOutcome status(String reference) {
        PvitTransaction transaction;
        try {
            transaction = api.payout(reference);
        } catch (PvitException e) {
            if (e.notFound()) {
                return PayoutOutcome.pending(null);
            }
            throw e;
        }
        if (!reference.equals(transaction.merchantReference())) {
            return PayoutOutcome.pending(null);
        }
        log.info("PVit payout {} status {}", reference, transaction.status());
        return switch (transaction.status()) {
            case SUCCESS -> PayoutOutcome.paid(transaction.providerReference() == null ? reference
                    : transaction.providerReference());
            case FAILED -> PayoutOutcome.failed("PAYOUT_DECLINED");
            case PENDING -> PayoutOutcome.pending(transaction.providerReference());
        };
    }
}
