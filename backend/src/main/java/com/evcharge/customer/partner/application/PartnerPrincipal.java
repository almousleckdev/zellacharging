package com.evcharge.customer.partner.application;

import java.util.UUID;

public record PartnerPrincipal(UUID keyId, String partner, String country) {
}
