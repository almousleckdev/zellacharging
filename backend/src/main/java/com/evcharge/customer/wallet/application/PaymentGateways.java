package com.evcharge.customer.wallet.application;

import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.TenantContext;
import com.evcharge.customer.wallet.application.port.PaymentGateway;
import com.evcharge.customer.wallet.domain.MobileOperator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentGateways {

    private final List<PaymentGateway> gateways;

    public PaymentGateways(List<PaymentGateway> gateways) {
        this.gateways = gateways;
    }

    public PaymentGateway forOperator(MobileOperator operator) {
        String country = TenantContext.require();
        return gateways.stream().filter(gateway -> gateway.supports(country, operator)).findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "OPERATOR_UNAVAILABLE",
                        "This operator is not available"));
    }
}
