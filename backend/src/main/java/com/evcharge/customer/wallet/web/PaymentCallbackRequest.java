package com.evcharge.customer.wallet.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
record PaymentCallbackRequest(String merchantReferenceId, Object transactionId, Object code) {
}
