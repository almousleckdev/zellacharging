package com.evcharge.customer.shared;

public interface HealthProbe {

    String name();

    ProbeResult check();
}
