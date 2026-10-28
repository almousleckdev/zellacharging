package com.evcharge.customer.admin.application;

import com.evcharge.customer.shared.TenantContext;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

final class CountryMerge {

    record Found<T>(long total, List<T> items) {
    }

    private CountryMerge() {
    }

    static <T> CountryListing<T> newestFirst(List<String> scope, PageWindow window, Function<Integer, Found<T>> search,
                                             Function<T, Instant> createdAt) {
        long total = 0;
        List<CountryEntry<T>> merged = new ArrayList<>();
        for (String code : scope) {
            Found<T> found = TenantContext.callAs(code, () -> search.apply(window.end()));
            total += found.total();
            found.items().forEach(item -> merged.add(new CountryEntry<>(code, item, createdAt.apply(item))));
        }
        merged.sort(Comparator.comparing((CountryEntry<T> entry) -> entry.createdAt()).reversed());
        int from = Math.min(window.offset(), merged.size());
        int to = Math.min(window.end(), merged.size());
        return new CountryListing<>(total, window.index(), window.size(), List.copyOf(merged.subList(from, to)));
    }
}
