package com.evcharge.customer.wallet;

import java.util.UUID;

public interface WithdrawalReviews {

    void approve(UUID adminId, UUID withdrawalId, String note);

    void reject(UUID adminId, UUID withdrawalId, String note);
}
