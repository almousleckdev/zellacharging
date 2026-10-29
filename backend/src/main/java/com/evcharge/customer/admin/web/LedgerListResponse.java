package com.evcharge.customer.admin.web;

import com.evcharge.customer.wallet.LedgerPage;
import com.evcharge.customer.wallet.LedgerRecord;

import java.util.List;

record LedgerListResponse(long total, int page, int size, String currency, int decimals, List<LedgerRecord> items) {

    static LedgerListResponse from(LedgerPage found, int page, int size) {
        return new LedgerListResponse(found.total(), page, size, found.currency(), found.decimals(), found.items());
    }
}
