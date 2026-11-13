package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.ConnectorOccupancy;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class ConnectorOccupancyService implements ConnectorOccupancy {

    private final SessionRepository sessions;
    private final ConnectorWait wait;

    public ConnectorOccupancyService(SessionRepository sessions, ConnectorWait wait) {
        this.sessions = sessions;
        this.wait = wait;
    }

    @Override
    public Map<String, Set<Integer>> busyConnectors() {
        return sessions.liveConnectors();
    }

    @Override
    public Map<String, Map<Integer, Integer>> minutesUntilFree() {
        Map<String, Map<Integer, Integer>> minutes = new HashMap<>();
        for (ChargingSession session : sessions.live()) {
            wait.minutesLeft(session).ifPresent(left -> minutes
                    .computeIfAbsent(session.stationId(), station -> new HashMap<>())
                    .merge(session.connectorId(), left, Math::max));
        }
        return minutes;
    }
}
