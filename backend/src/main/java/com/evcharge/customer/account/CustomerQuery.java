package com.evcharge.customer.account;

import com.evcharge.customer.shared.Sort;

import java.util.Collection;
import java.util.UUID;

public record CustomerQuery(String text, String status, Collection<UUID> onlyIds, Sort sort, int limit, int offset) {
}
