package com.evcharge.customer.admin.application;

import com.evcharge.customer.shared.ProbeResult;

public record ProbeReport(String name, ProbeResult result) {
}
