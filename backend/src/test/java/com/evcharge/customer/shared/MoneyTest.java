package com.evcharge.customer.shared;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void energyPriceRoundsHalfUpToMinorUnits() {
        assertThat(Money.forEnergy(new BigDecimal("0.166"), 150, "XAF", 0).minor()).isEqualTo(25);
        assertThat(Money.forEnergy(new BigDecimal("0.010"), 150, "XAF", 0).minor()).isEqualTo(2); // 1.5 -> 2
        assertThat(Money.forEnergy(new BigDecimal("0.003"), 150, "XAF", 0).minor()).isZero();    // 0.45 -> 0
    }

    @Test
    void threeDecimalCurrencyIsNotDividedByHundred() {
        Money tnd = new Money(12_345, "TND", 3);
        assertThat(tnd.toMajor()).isEqualByComparingTo("12.345");
        assertThat(new Money(1500, "XAF", 0).toMajor()).isEqualByComparingTo("1500");
    }

    @Test
    void energyBoughtByAnAmountRoundsDown() {
        assertThat(new Money(1000, "XAF", 0).energyAt(150)).isEqualByComparingTo("6.666");
    }

    @Test
    void arithmeticRejectsCurrencyMismatchAndNegativeResults() {
        Money xaf = new Money(100, "XAF", 0);
        assertThat(xaf.plus(new Money(50, "XAF", 0)).minor()).isEqualTo(150);
        assertThat(xaf.min(new Money(40, "XAF", 0)).minor()).isEqualTo(40);
        assertThatThrownBy(() -> xaf.plus(new Money(1, "TND", 3))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> xaf.minus(new Money(101, "XAF", 0))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsInvalidConstruction() {
        assertThatThrownBy(() -> new Money(-1, "XAF", 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Money(1, "xaf", 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Money(1, "XAF", 4)).isInstanceOf(IllegalArgumentException.class);
    }
}
