package com.evcharge.customer.charging.application;

import com.evcharge.customer.shared.Country;
import com.evcharge.customer.shared.CountryDirectory;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FreeWindowTimesTest {

    private final CountryDirectory countries = mock(CountryDirectory.class);
    private final FreeWindowTimes times = new FreeWindowTimes(countries);

    private String display(String instant) {
        when(countries.activeByCode("GA")).thenReturn(Optional.of(
                new Country("GA", "Gabon", "XAF", 0, "GA", "Africa/Libreville")));
        return TenantContext.callAs("GA", () -> times.display(Instant.parse(instant)));
    }

    @Test
    void anEndInTheEveningIsShownInTheCountryTimeZone() {
        assertThat(display("2026-11-22T21:00:00Z")).isEqualTo("22:00");
    }

    @Test
    void anEndInTheMiddleOfTheDayKeepsItsMinutes() {
        assertThat(display("2026-11-22T14:30:00Z")).isEqualTo("15:30");
    }

    @Test
    void anEndAtTheNextMidnightIsShownAsTheLastMinuteOfTheDay() {
        assertThat(display("2026-11-22T23:00:00Z")).isEqualTo("23:59");
    }

    @Test
    void anUnknownCountryFallsBackToUniversalTime() {
        when(countries.activeByCode("GA")).thenReturn(Optional.empty());

        assertThat(TenantContext.callAs("GA", () -> times.display(Instant.parse("2026-11-22T21:00:00Z"))))
                .isEqualTo("21:00");
    }
}
