package com.evcharge.customer.partner.web;

import com.evcharge.customer.partner.application.LookupResult;

import java.util.List;

record LookupResponse(List<ResultView> results) {

    record ResultView(String ref, List<MatchView> matches) {
    }

    static LookupResponse of(List<LookupResult> results) {
        return new LookupResponse(results.stream().map(result -> new ResultView(result.ref(),
                result.matches().stream().map(MatchView::of).toList())).toList());
    }
}
