package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.application.FeeCalculator;
import com.evcharge.customer.charging.application.port.SessionCharges;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargeState;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.FeeBreakdown;
import com.evcharge.customer.charging.domain.SessionCharge;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.Country;
import com.evcharge.customer.shared.CountryDirectory;
import com.evcharge.customer.shared.TenantContext;
import com.evcharge.customer.wallet.WalletFunding;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class SessionChargeBackfill implements ApplicationRunner {

    private static final int BATCH = 500;

    private final SessionCharges charges;
    private final SessionRepository sessions;
    private final FeeCalculator fees;
    private final WalletFunding funding;
    private final CountryDirectory countries;
    private final Clock clock;

    SessionChargeBackfill(SessionCharges charges, SessionRepository sessions, FeeCalculator fees,
                          WalletFunding funding, CountryDirectory countries, Clock clock) {
        this.charges = charges;
        this.sessions = sessions;
        this.fees = fees;
        this.funding = funding;
        this.countries = countries;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        backfillAll();
    }

    public int backfillAll() {
        int written = 0;
        for (Country country : countries.active()) {
            written += TenantContext.callAs(country.code(), this::backfillCountry);
        }
        return written;
    }

    private int backfillCountry() {
        int written = 0;
        Set<UUID> stillSettling = new HashSet<>(funding.openHoldSessions());
        List<UUID> batch = charges.terminalSessionsWithoutCharge(BATCH, stillSettling);
        while (!batch.isEmpty()) {
            batch.forEach(id -> sessions.findById(id).ifPresent(this::backfill));
            written += batch.size();
            batch = charges.terminalSessionsWithoutCharge(BATCH, stillSettling);
        }
        return written;
    }

    private void backfill(ChargingSession session) {
        charges.insertIfAbsent(chargeOf(session));
    }

    private SessionCharge chargeOf(ChargingSession session) {
        if (session.state() == SessionState.FAILED) {
            return SessionCharge.withoutAmounts(session, ChargeState.NOT_CHARGED, true, clock.instant());
        }
        if (session.costMinor() == null) {
            return SessionCharge.withoutAmounts(session, ChargeState.UNDER_REVIEW, true, clock.instant());
        }
        FeeBreakdown breakdown = fees.of(session.costMinor());
        long captured = funding.captured(session.id()).orElse(breakdown.totalMinor());
        return SessionCharge.settled(session, breakdown, captured, true, clock.instant());
    }
}
