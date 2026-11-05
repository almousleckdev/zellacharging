package com.evcharge.customer.partner.web;

import com.evcharge.customer.partner.application.LookupMatch;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

record MatchView(UUID customerId, String idTag, List<String> matchedOn, String quality,
                 @JsonInclude(JsonInclude.Include.NON_NULL) String firstName,
                 @JsonInclude(JsonInclude.Include.NON_NULL) String lastName) {

    static MatchView of(LookupMatch match) {
        return new MatchView(match.customerId(), match.idTag(), match.matchedOn(), match.quality().name(),
                match.firstName(), match.lastName());
    }
}
