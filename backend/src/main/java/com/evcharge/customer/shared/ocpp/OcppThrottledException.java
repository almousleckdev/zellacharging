package com.evcharge.customer.shared.ocpp;

import java.io.IOException;

class OcppThrottledException extends IOException {

    OcppThrottledException() {
        super("The charging platform request budget is used up");
    }
}
