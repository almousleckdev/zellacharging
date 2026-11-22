package com.evcharge.customer.charging.web;

import com.evcharge.customer.shared.CustomerPrincipal;
import com.evcharge.customer.shared.FreeSundayAccess;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/free-sunday")
class FreeSundayController {

    private final FreeSundayAccess freeSunday;

    FreeSundayController(FreeSundayAccess freeSunday) {
        this.freeSunday = freeSunday;
    }

    @GetMapping
    FreeSundayResponse offer(CustomerPrincipal me) {
        return FreeSundayResponse.from(freeSunday.offer(me.id()));
    }
}
