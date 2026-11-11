package com.evcharge.customer.shared;

import java.util.List;

public interface SuspensionEvidenceFeed {

    List<SuspensionEvidenceRow> recent(int limit);
}
