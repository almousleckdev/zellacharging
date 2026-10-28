package com.evcharge.customer.wallet.web;

import com.evcharge.customer.wallet.application.TopupSettlement;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/payments")
class PaymentCallbackController {

    private final TopupSettlement settlement;

    PaymentCallbackController(TopupSettlement settlement) {
        this.settlement = settlement;
    }

    @PostMapping("/callback")
    PaymentCallbackAcknowledgement callback(@RequestBody PaymentCallbackRequest request) {
        settlement.settleByReference(request.merchantReferenceId());
        return PaymentCallbackAcknowledgement.echoing(request);
    }
}
