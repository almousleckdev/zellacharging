package com.evcharge.customer.account;

import java.util.UUID;

public record CustomerMatch(UUID id, String firstName, String lastName, String email, String phoneE164) {
}
