package com.evcharge.customer.admin.application;

import com.evcharge.customer.account.CustomerRecord;
import com.evcharge.customer.charging.SessionRecord;
import com.evcharge.customer.shared.Sort;
import com.evcharge.customer.wallet.PaymentRecord;

import java.util.Comparator;
import java.util.Set;

final class SortOrders {

    static final Set<String> SESSION_FIELDS = Set.of("date", "amount", "energy");
    static final Set<String> PAYMENT_FIELDS = Set.of("date", "amount");
    static final Set<String> CUSTOMER_FIELDS = Set.of("date", "name");

    private SortOrders() {
    }

    static Comparator<SessionRecord> sessions(Sort sort) {
        Comparator<SessionRecord> order = switch (sort.field()) {
            case "amount" -> Comparator.comparing(SessionRecord::costMinor, Comparator.nullsFirst(Comparator.naturalOrder()));
            case "energy" -> Comparator.comparing(SessionRecord::energyKwh, Comparator.nullsFirst(Comparator.naturalOrder()));
            default -> Comparator.comparing(SessionRecord::createdAt);
        };
        return directed(order, sort);
    }

    static Comparator<PaymentRecord> payments(Sort sort) {
        Comparator<PaymentRecord> order = "amount".equals(sort.field())
                ? Comparator.comparing(PaymentRecord::amountMinor) : Comparator.comparing(PaymentRecord::createdAt);
        return directed(order, sort);
    }

    static Comparator<CustomerRecord> customers(Sort sort) {
        Comparator<CustomerRecord> order = "name".equals(sort.field())
                ? Comparator.comparing((CustomerRecord customer) -> (customer.firstName() + " " + customer.lastName()).toLowerCase())
                : Comparator.comparing(CustomerRecord::createdAt);
        return directed(order, sort);
    }

    private static <T> Comparator<T> directed(Comparator<T> order, Sort sort) {
        return sort.descending() ? order.reversed() : order;
    }
}
