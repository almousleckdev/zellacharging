package com.evcharge.customer.partner.application;

import java.util.List;

public record LookupResult(String ref, List<LookupMatch> matches) {
}
