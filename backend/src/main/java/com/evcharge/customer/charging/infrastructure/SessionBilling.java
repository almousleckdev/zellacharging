package com.evcharge.customer.charging.infrastructure;

final class SessionBilling {

    static final String FREE = "exists (select 1 from funding_hold h where h.session_id = charging_session.id "
            + "and h.waived)";
    static final String PAID = "not " + FREE;

    private SessionBilling() {
    }
}
