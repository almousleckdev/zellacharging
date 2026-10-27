package com.evcharge.customer.wallet.application;

import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.TenantContext;
import com.evcharge.customer.wallet.application.port.PayoutGateway;
import com.evcharge.customer.wallet.domain.MobileOperator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PayoutGateways {

    private final List<PayoutGateway> gateways;

    public PayoutGateways(List<PayoutGateway> gateways) {
        this.gateways = gateways;
    }

    public PayoutGateway forOperator(MobileOperator operator) {
        String country = TenantContext.require();
        return gateways.stream().filter(gateway -> gateway.supports(country, operator)).findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "WITHDRAWALS_UNAVAILABLE",
                        "Withdrawals are not available for this operator"));
    }
}
