package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.ConnectorOccupancy;
import com.evcharge.customer.charging.application.port.SessionRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class ConnectorOccupancyService implements ConnectorOccupancy {

    private final SessionRepository sessions;

    public ConnectorOccupancyService(SessionRepository sessions) {
        this.sessions = sessions;
    }

    @Override
    public Map<String, Set<Integer>> busyConnectors() {
        return sessions.liveConnectors();
    }
}
