package com.evcharge.customer.shared;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConstantTimeTest {

    @Test
    void equalTextsMatch() {
        assertThat(ConstantTime.equal("a1b2c3", "a1b2c3")).isTrue();
    }

    @Test
    void differentTextsOfTheSameLengthDoNotMatch() {
        assertThat(ConstantTime.equal("a1b2c3", "a1b2c4")).isFalse();
    }

    @Test
    void textsOfDifferentLengthsDoNotMatch() {
        assertThat(ConstantTime.equal("a1b2c3", "a1b2c")).isFalse();
        assertThat(ConstantTime.equal("", "a")).isFalse();
    }

    @Test
    void aMissingTextNeverMatches() {
        assertThat(ConstantTime.equal(null, "a")).isFalse();
        assertThat(ConstantTime.equal("a", null)).isFalse();
        assertThat(ConstantTime.equal(null, null)).isFalse();
    }
}
