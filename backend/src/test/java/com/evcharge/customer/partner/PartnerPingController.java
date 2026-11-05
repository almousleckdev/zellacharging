package com.evcharge.customer.partner;

import com.evcharge.customer.shared.TenantContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PartnerPingController {

    @GetMapping("/v1/partners/ping")
    String ping() {
        return TenantContext.require();
    }
}
