package com.evcharge.customer.wallet.infrastructure.pvit;

import java.math.BigDecimal;

record PvitTransaction(PvitStatus status, String merchantReference, String providerReference, BigDecimal amount) {
}
