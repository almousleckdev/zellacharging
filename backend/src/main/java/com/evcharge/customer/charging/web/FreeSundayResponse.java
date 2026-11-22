package com.evcharge.customer.charging.web;

import com.evcharge.customer.shared.FreeSundayOffer;
import com.evcharge.customer.shared.FreeSundayWindow;

import java.time.Instant;

record FreeSundayResponse(boolean available, String reason, Instant endsAt, FreeSundayWindow window) {

    static FreeSundayResponse from(FreeSundayOffer offer) {
        return new FreeSundayResponse(offer.available(), offer.reason(), offer.endsAt(), offer.window());
    }
}
