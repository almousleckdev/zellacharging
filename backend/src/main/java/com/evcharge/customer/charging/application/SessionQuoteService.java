package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.StartableConnectors;
import com.evcharge.customer.charging.domain.SessionQuote;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.wallet.StartLimits;
import com.evcharge.customer.wallet.WalletFunding;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class SessionQuoteService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final StartableConnectors connectors;
    private final WalletFunding funding;
    private final FeeRates fees;
    private final PriceList prices;

    public SessionQuoteService(StartableConnectors connectors, WalletFunding funding, FeeRates fees,
                               PriceList prices) {
        this.connectors = connectors;
        this.funding = funding;
        this.fees = fees;
        this.prices = prices;
    }

    public SessionQuote quote(UUID customerId, String stationId, int connectorId, long amountMinor) {
        connectors.require(stationId, connectorId);
        StartLimits limits = funding.startLimits();
        if (amountMinor < limits.minimumMinor() || amountMinor > limits.maximumMinor()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT", "Amount is outside the allowed range");
        }
        long available = funding.available(customerId).minor();
        return new SessionQuote(amountMinor, limits.currency(), limits.decimals(), available,
                Math.max(0, amountMinor - available), limits.minimumMinor(), limits.maximumMinor(),
                fees.vatPercent(), fees.serviceFeePercent(), prices.pricePerKwhMinor(), energyFor(amountMinor));
    }

    private BigDecimal energyFor(long amountMinor) {
        Long price = prices.pricePerKwhMinor();
        if (price == null || price <= 0) {
            return null;
        }
        BigDecimal multiplier = BigDecimal.ONE.add(fees.vatPercent().add(fees.serviceFeePercent())
                .divide(HUNDRED, 6, RoundingMode.HALF_UP));
        return BigDecimal.valueOf(amountMinor)
                .divide(multiplier.multiply(BigDecimal.valueOf(price)), 3, RoundingMode.DOWN);
    }
}
