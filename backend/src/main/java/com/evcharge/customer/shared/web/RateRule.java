package com.evcharge.customer.shared.web;

import java.time.Duration;

record RateRule(String method, String path, int limit, Duration window, boolean perCustomer) {

    String bucket() {
        return method + " " + path;
    }

    boolean matches(String requestMethod, String requestPath) {
        return method.equals(requestMethod) && path.equals(requestPath);
    }
}
