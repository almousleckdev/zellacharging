package com.evcharge.customer.charging;

import com.evcharge.customer.charging.application.FeeCalculator;
import com.evcharge.customer.charging.application.FeeSettings;
import com.evcharge.customer.charging.domain.FeeBreakdown;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FeeCalculatorTest {

    private FeeCalculator calculator(String vat, String service) {
        return new FeeCalculator(new FeeSettings(new BigDecimal(vat), new BigDecimal(service)));
    }

    @Test
    void addsVatAndServiceFeeOnTopOfTheElectricFee() {
        FeeBreakdown fees = calculator("18", "5").of(1000);

        assertThat(fees.electricMinor()).isEqualTo(1000);
        assertThat(fees.vatMinor()).isEqualTo(180);
        assertThat(fees.serviceMinor()).isEqualTo(50);
        assertThat(fees.totalMinor()).isEqualTo(1230);
    }

    @Test
    void roundsEachFeeToTheNearestMinorUnit() {
        FeeBreakdown fees = calculator("18", "0").of(125);

        assertThat(fees.vatMinor()).isEqualTo(23);
    }

    @Test
    void chargesNothingExtraWhenNoRatesAreConfigured() {
        FeeBreakdown fees = new FeeCalculator(new FeeSettings(null, null)).of(300);

        assertThat(fees.vatMinor()).isZero();
        assertThat(fees.serviceMinor()).isZero();
        assertThat(fees.totalMinor()).isEqualTo(300);
    }

    @Test
    void theTwoPercentZellaServiceFeeIsEarnedOnTheElectricFeeOnly() {
        FeeBreakdown fees = calculator("0", "2").of(10000);

        assertThat(fees.serviceMinor()).isEqualTo(200);
        assertThat(fees.totalMinor()).isEqualTo(10200);
    }

    @Test
    void theServiceFeeRoundsToTheNearestFranc() {
        assertThat(calculator("0", "2").of(125).serviceMinor()).isEqualTo(3);
        assertThat(calculator("0", "2").of(124).serviceMinor()).isEqualTo(2);
    }

    @Test
    void theElectricShareThatFitsATotalNeverExceedsIt() {
        assertThat(calculator("0", "0").electricWithin(7000)).isEqualTo(7000);
        assertThat(calculator("18", "2").electricWithin(7000)).isEqualTo(5833);
        assertThat(calculator("18", "2").of(5833).totalMinor()).isLessThanOrEqualTo(7000);
    }
}
