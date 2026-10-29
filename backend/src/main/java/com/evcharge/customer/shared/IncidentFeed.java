package com.evcharge.customer.shared;

import java.util.List;

public interface IncidentFeed {

    List<Incident> recent(LogFilter filter, int limit);
}
