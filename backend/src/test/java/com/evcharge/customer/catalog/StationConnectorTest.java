package com.evcharge.customer.catalog;

import com.evcharge.customer.catalog.domain.StationConnector;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StationConnectorTest {

    private static StationConnector with(String status) {
        return new StationConnector(1, status, "CCS2", null);
    }

    @Test
    void aPluggedInConnectorCanBeStartedButIsNotCountedAsFree() {
        assertThat(with("Preparing").isStartable()).isTrue();
        assertThat(with("Preparing").isAvailable()).isFalse();
    }

    @Test
    void onlyFreeOrPluggedInConnectorsAreStartable() {
        assertThat(with("Available").isStartable()).isTrue();
        for (String status : new String[]{"Charging", "SuspendedEV", "SuspendedEVSE", "Finishing", "Reserved",
                "Unavailable", "Faulted", "Unknown"}) {
            assertThat(with(status).isStartable()).as(status).isFalse();
        }
    }
}
