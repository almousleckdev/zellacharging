package com.evcharge.customer.account.web;

import com.evcharge.customer.account.application.SupportSettings;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/support")
class SupportController {

    private final SupportSettings settings;

    SupportController(SupportSettings settings) {
        this.settings = settings;
    }

    @GetMapping
    SupportResponse support() {
        return SupportResponse.from(settings);
    }
}
