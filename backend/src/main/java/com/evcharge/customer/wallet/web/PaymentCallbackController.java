package com.evcharge.customer.wallet.web;

import com.evcharge.customer.wallet.application.TopupSettlement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/payments")
class PaymentCallbackController {

    private final TopupSettlement settlement;

    PaymentCallbackController(TopupSettlement settlement) {
        this.settlement = settlement;
    }

    @PostMapping("/callback")
    ResponseEntity<Map<String, Object>> callback(@RequestBody PaymentCallbackRequest request) {
        settlement.settleByReference(request.merchantReferenceId());
        return ResponseEntity.ok(Map.of("transactionId", String.valueOf(request.transactionId()),
                "responseCode", String.valueOf(request.code())));
    }
}
