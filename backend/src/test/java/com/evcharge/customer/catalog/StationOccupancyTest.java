package com.evcharge.customer.catalog;

import com.evcharge.customer.catalog.domain.Station;
import com.evcharge.customer.catalog.domain.StationConnector;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class StationOccupancyTest {

    private Station station(String first, String second) {
        return new Station("CP-1", "GA", true, null, null, List.of(
                new StationConnector(1, first, "Type2", new BigDecimal("22")),
                new StationConnector(2, second, "Type2", new BigDecimal("22"))));
    }

    @Test
    void aConnectorWithALiveSessionShowsAsInUseEvenIfThePlatformSaysAvailable() {
        Station marked = station("Available", "Available").occupying(Set.of(1));

        assertThat(marked.connector(1).orElseThrow().status()).isEqualTo("Charging");
        assertThat(marked.connector(2).orElseThrow().isAvailable()).isTrue();
        assertThat(marked.availableConnectors()).isEqualTo(1);
    }

    @Test
    void aFaultedConnectorKeepsItsRealStatus() {
        Station marked = station("Faulted", "Available").occupying(Set.of(1));

        assertThat(marked.connector(1).orElseThrow().status()).isEqualTo("Faulted");
    }

    @Test
    void nothingChangesWithoutLiveSessions() {
        Station original = station("Available", "Available");

        assertThat(original.occupying(null)).isSameAs(original);
        assertThat(original.occupying(Set.of())).isSameAs(original);
    }
}
