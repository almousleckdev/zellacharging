package com.evcharge.customer.charging.infrastructure;

final class SessionBilling {

    static final String FREE = "exists (select 1 from funding_hold h where h.session_id = charging_session.id "
            + "and h.waived)";
    static final String FREE_CATEGORY = "(select case when exists (select 1 from free_sunday_claim f "
            + "where f.session_id = h.session_id and f.state = 'CLAIMED') then 'FREE_SUNDAY' else "
            + "(select g.category from customer_complimentary g where g.customer_id = h.customer_id "
            + "and g.country_code = s.country_code) end from funding_hold h "
            + "where h.session_id = s.id and h.waived) as free_category";
    static final String PAID = "not " + FREE;
    static final String NDZELA_DRIVER = "exists (select 1 from partner_link l where l.partner = 'NDZELA' "
            + "and l.customer_id = s.customer_id) as ndzela_driver";

    private SessionBilling() {
    }
}
