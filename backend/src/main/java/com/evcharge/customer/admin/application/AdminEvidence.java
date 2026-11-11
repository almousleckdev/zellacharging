package com.evcharge.customer.admin.application;

import com.evcharge.customer.shared.SuspensionEvidenceRow;

public record AdminEvidence(String country, SuspensionEvidenceRow row) {
}
