package com.evcharge.customer.charging;

import com.evcharge.customer.charging.application.IdTagService;
import com.evcharge.customer.charging.application.StartCommander;
import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.CommandOutcome;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StartCommanderTest {

    private final ChargingGateway gateway = mock(ChargingGateway.class);
    private final IdTagService idTags = mock(IdTagService.class);
    private final SessionRepository sessions = mock(SessionRepository.class);
    private final StartCommander starter = new StartCommander(gateway, idTags, sessions);
    private final ChargingSession session = ChargingSession.requested(UUID.randomUUID(), UUID.randomUUID(), "CP-1", 1,
            "Z-OLD", Instant.parse("2026-11-09T10:00:00Z"));

    @Test
    void anAcceptedStartKeepsTheIdTag() {
        when(gateway.start("CP-1", 1, "Z-OLD", "start:" + session.id())).thenReturn(CommandOutcome.success());

        StartCommander.Started started = starter.issue(session);

        assertThat(started.outcome().accepted()).isTrue();
        assertThat(started.session().idTag()).isEqualTo("Z-OLD");
        verify(idTags, never()).replace(session.customerId());
    }

    @Test
    void anIdTagStillHeldByAnOpenChargeIsReplacedAndTheStartIsRetriedOnce() {
        when(gateway.start("CP-1", 1, "Z-OLD", "start:" + session.id()))
                .thenReturn(CommandOutcome.refused("CONCURRENT_IDTAG", null));
        when(idTags.replace(session.customerId())).thenReturn("Z-NEW");
        when(gateway.start("CP-1", 1, "Z-NEW", "start:" + session.id() + ":fresh")).thenReturn(CommandOutcome.success());

        StartCommander.Started started = starter.issue(session);

        assertThat(started.outcome().accepted()).isTrue();
        assertThat(started.session().idTag()).isEqualTo("Z-NEW");
        verify(sessions).reassignIdTag(session.id(), "Z-NEW");
    }

    @Test
    void otherRefusalsAreNotRetried() {
        when(gateway.start("CP-1", 1, "Z-OLD", "start:" + session.id()))
                .thenReturn(CommandOutcome.refused("BLOCKED_IDTAG", null));

        StartCommander.Started started = starter.issue(session);

        assertThat(started.outcome().reasonCode()).isEqualTo("BLOCKED_IDTAG");
        verify(idTags, never()).replace(session.customerId());
        verify(gateway, times(1)).start(anyString(), anyInt(), anyString(), anyString());
    }
}
