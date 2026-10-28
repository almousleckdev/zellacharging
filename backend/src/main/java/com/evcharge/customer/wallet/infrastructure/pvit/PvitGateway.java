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

    private final MobileOperator operator;
    private final PvitApi api;
    private final PvitKyc kyc;

    PvitGateway(MobileOperator operator, PvitApi api, PvitKyc kyc) {
        this.operator = operator;
        this.api = api;
        this.kyc = kyc;
    }

    @Override
    public boolean supports(String country, MobileOperator operator) {
        return this.operator == operator && PvitOperators.codeOf(country, operator).isPresent();
    }

    @Override
    public PaymentOutcome collect(CollectionRequest request) {
        String country = request.reference().substring(0, 2);
        String operatorCode = PvitOperators.codeOf(country, request.operator()).orElseThrow();
        try {
            String customerNumber = PvitPhone.nationalFormat(request.msisdn());
            kyc.inspect(operatorCode, customerNumber);
            PvitStatus status = api.startPayment(request.reference(), BigDecimal.valueOf(request.amountMinor()),
                    operatorCode, customerNumber, DESCRIPTION);
            return status == PvitStatus.FAILED ? PaymentOutcome.failed("PROVIDER_REFUSED") : PaymentOutcome.pending(null);
        } catch (PvitException e) {
            if (e.refused()) {
                log.warn("PVit refused payment {}: {}", request.reference(), e.getMessage());
                return PaymentOutcome.failed(e.failureCode() == null ? "PROVIDER_REFUSED" : e.failureCode());
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
            case FAILED -> declined(reference, transaction);
            case PENDING -> PaymentOutcome.pending(transaction.providerReference());
        };
    }

    private static PaymentOutcome declined(String reference, PvitTransaction transaction) {
        log.warn("PVit payment {} declined, provider answer: {}", reference, transaction.answer());
        return PaymentOutcome.failed("PAYMENT_DECLINED");
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
