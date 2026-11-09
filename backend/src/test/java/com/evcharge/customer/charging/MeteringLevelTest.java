package com.evcharge.customer.charging;

import com.evcharge.customer.charging.domain.Metering;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MeteringLevelTest {

    private static Metering withLevel(String level) {
        return new Metering(new BigDecimal("1.2"), new BigDecimal("7"), level == null ? null : new BigDecimal(level),
                60L, null, null, Instant.EPOCH);
    }

    @Test
    void aLevelOfZeroOrAboveOneHundredIsDroppedButTheEnergyIsKept() {
        assertThat(withLevel("0").stateOfChargePercent()).isNull();
        assertThat(withLevel("-3").stateOfChargePercent()).isNull();
        assertThat(withLevel("150").stateOfChargePercent()).isNull();
        assertThat(withLevel("0").totalKwh()).isEqualByComparingTo("1.2");
    }

    @Test
    void aRealLevelIsKept() {
        assertThat(withLevel("57").stateOfChargePercent()).isEqualByComparingTo("57");
        assertThat(withLevel("100").stateOfChargePercent()).isEqualByComparingTo("100");
        assertThat(withLevel(null).stateOfChargePercent()).isNull();
    }
}
