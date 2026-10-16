package com.evcharge.customer.wallet.application.port;

import com.evcharge.customer.wallet.domain.FundingHold;
import com.evcharge.customer.wallet.domain.HoldState;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HoldRepository {

    void insert(FundingHold hold);

    Optional<FundingHold> lock(UUID sessionId);

    Optional<FundingHold> find(UUID sessionId);

    void close(UUID sessionId, HoldState state, long capturedMinor);

    List<UUID> openSessionIds();
}
