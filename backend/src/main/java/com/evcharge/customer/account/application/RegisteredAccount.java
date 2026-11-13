package com.evcharge.customer.account.application;

import com.evcharge.customer.account.domain.ContactPoint;

import java.util.UUID;

public record RegisteredAccount(UUID customerId, ContactPoint toVerify) {
}
