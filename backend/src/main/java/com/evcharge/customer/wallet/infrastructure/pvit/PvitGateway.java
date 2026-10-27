package com.evcharge.customer.wallet.infrastructure.pvit;

import com.evcharge.customer.wallet.application.port.PaymentGateway;
import com.evcharge.customer.wallet.domain.CollectionRequest;
import com.evcharge.customer.wallet.domain.MobileOperator;
import com.evcharge.customer.wallet.domain.PaymentOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

class PvitGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(PvitGateway.class);
    private static final String DESCRIPTION = "Zella Charging";

    private final PvitApi api;

    PvitGateway(PvitApi api) {
        this.api = api;
    }

    @Override
    public boolean supports(String country, MobileOperator operator) {
        return PvitOperators.codeOf(country, operator).isPresent();
    }

    @Override
    public PaymentOutcome collect(CollectionRequest request) {
        String country = request.reference().substring(0, 2);
        String operatorCode = PvitOperators.codeOf(country, request.operator()).orElseThrow();
        try {
            PvitStatus status = api.startPayment(request.reference(), BigDecimal.valueOf(request.amountMinor()),
                    operatorCode, PvitPhone.nationalFormat(request.msisdn()), DESCRIPTION);
            return status == PvitStatus.FAILED ? PaymentOutcome.failed("PROVIDER_REFUSED") : PaymentOutcome.pending(null);
        } catch (PvitException e) {
            if (e.refused()) {
                log.warn("PVit refused payment {}: {}", request.reference(), e.getMessage());
                return PaymentOutcome.failed("PROVIDER_REFUSED");
            }
            throw e;
        }
    }

    @Override
    public PaymentOutcome status(String reference) {
        PvitTransaction transaction;
        try {
            transaction = api.transaction(reference);
        } catch (PvitException e) {
            if (e.notFound()) {
                return PaymentOutcome.pending(null);
            }
            throw e;
        }
        if (!reference.equals(transaction.merchantReference())) {
            return PaymentOutcome.pending(null);
        }
        log.info("PVit payment {} status {} amount {}", reference, transaction.status(), transaction.amount());
        return switch (transaction.status()) {
            case SUCCESS -> confirmed(transaction);
            case FAILED -> PaymentOutcome.failed("PAYMENT_DECLINED");
            case PENDING -> PaymentOutcome.pending(transaction.providerReference());
        };
    }

    private static PaymentOutcome confirmed(PvitTransaction transaction) {
        if (transaction.amount() == null || transaction.amount().signum() <= 0) {
            return PaymentOutcome.pending(transaction.providerReference());
        }
        long minor = transaction.amount().setScale(0, RoundingMode.DOWN).longValueExact();
        String providerRef = transaction.providerReference() == null ? transaction.merchantReference()
                : transaction.providerReference();
        return PaymentOutcome.succeeded(providerRef, minor);
    }
}
