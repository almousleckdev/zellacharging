package com.evcharge.customer.shared;

public record ProbeResult(boolean up, long millis, String detail) {
}
