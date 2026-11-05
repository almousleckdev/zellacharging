package com.evcharge.customer.partner.application;

import com.evcharge.customer.partner.domain.MatchQuality;

import java.util.List;
import java.util.UUID;

public record LookupMatch(UUID customerId, String idTag, List<String> matchedOn, MatchQuality quality,
                          String firstName, String lastName) {
}
