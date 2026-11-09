package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.CommandOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StartCommander {

    public record Started(ChargingSession session, CommandOutcome outcome) {
    }

    private static final Logger log = LoggerFactory.getLogger(StartCommander.class);
    private static final String STILL_IN_USE = "CONCURRENT_IDTAG";

    private final ChargingGateway gateway;
    private final IdTagService idTags;
    private final SessionRepository sessions;

    public StartCommander(ChargingGateway gateway, IdTagService idTags, SessionRepository sessions) {
        this.gateway = gateway;
        this.idTags = idTags;
        this.sessions = sessions;
    }

    public Started issue(ChargingSession session) {
        CommandOutcome outcome = send(session, "start:" + session.id());
        if (outcome.accepted() || !STILL_IN_USE.equals(outcome.reasonCode())) {
            return new Started(session, outcome);
        }
        ChargingSession renewed = withFreshIdTag(session);
        return new Started(renewed, send(renewed, "start:" + session.id() + ":fresh"));
    }

    private ChargingSession withFreshIdTag(ChargingSession session) {
        String idTag = idTags.replace(session.customerId());
        sessions.reassignIdTag(session.id(), idTag);
        log.warn("ALERT the platform still holds an open charge for the old id tag of session {}: using a new one",
                session.id());
        return session.withIdTag(idTag);
    }

    private CommandOutcome send(ChargingSession session, String idempotencyKey) {
        return gateway.start(session.stationId(), session.connectorId(), session.idTag(), idempotencyKey);
    }
}
