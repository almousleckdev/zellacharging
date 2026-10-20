package com.evcharge.customer.charging.infrastructure;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OcppMeteringPayloadTest {

    private BigDecimal levelOf(String level) {
        return new OcppMeteringPayload(BigDecimal.ONE, null, new BigDecimal(level), 10L, null, null, null)
                .toDomain().stateOfChargePercent();
    }

    @Test
    void keepsARealBatteryLevel() {
        assertThat(levelOf("40")).isEqualByComparingTo("40");
        assertThat(levelOf("100")).isEqualByComparingTo("100");
    }

    @Test
    void treatsZeroAsAPlaceholderNotAReading() {
        assertThat(levelOf("0")).isNull();
    }

    @Test
    void dropsAnImpossibleLevel() {
        assertThat(levelOf("101")).isNull();
    }
}
