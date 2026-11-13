package com.evcharge.customer.catalog;

import com.evcharge.customer.catalog.domain.Station;
import com.evcharge.customer.catalog.domain.StationConnector;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class StationOccupancyTest {

    private Station station(String first, String second) {
        return new Station("CP-1", "GA", null, null, true, null, null, null, List.of(
                new StationConnector(1, first, "Type2", new BigDecimal("22")),
                new StationConnector(2, second, "Type2", new BigDecimal("22"))));
    }

    @Test
    void aConnectorWithALiveSessionShowsAsInUseEvenIfThePlatformSaysAvailable() {
        Station marked = station("Available", "Available").occupying(Set.of(1), null);

        assertThat(marked.connector(1).orElseThrow().status()).isEqualTo("Charging");
        assertThat(marked.connector(2).orElseThrow().isAvailable()).isTrue();
        assertThat(marked.availableConnectors()).isEqualTo(1);
    }

    @Test
    void aFaultedConnectorKeepsItsRealStatus() {
        Station marked = station("Faulted", "Available").occupying(Set.of(1), null);

        assertThat(marked.connector(1).orElseThrow().status()).isEqualTo("Faulted");
    }

    @Test
    void aBusyConnectorShowsHowManyMinutesAreLeft() {
        Station marked = station("Available", "Charging").occupying(Set.of(1, 2), Map.of(1, 12, 2, 30));

        assertThat(marked.connector(1).orElseThrow().freeInMinutes()).isEqualTo(12);
        assertThat(marked.connector(2).orElseThrow().freeInMinutes()).isEqualTo(30);
    }

    @Test
    void aFaultedConnectorNeverPromisesAFreeTime() {
        Station marked = station("Faulted", "Available").occupying(Set.of(1), Map.of(1, 12));

        assertThat(marked.connector(1).orElseThrow().freeInMinutes()).isNull();
    }

    @Test
    void nothingChangesWithoutLiveSessions() {
        Station original = station("Available", "Available");

        assertThat(original.occupying(null, null)).isSameAs(original);
        assertThat(original.occupying(Set.of(), null)).isSameAs(original);
    }
}
