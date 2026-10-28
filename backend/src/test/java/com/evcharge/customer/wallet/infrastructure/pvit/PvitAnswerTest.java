package com.evcharge.customer.wallet.infrastructure.pvit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PvitAnswerTest {

    @Test
    void longNumbersAreHiddenSoNoPhoneNumberReachesTheLogs() {
        String masked = PvitAnswer.masked("{\"status\":\"FAILED\",\"customerID\":\"077092209\",\"amount\":500}");

        assertThat(masked).contains("FAILED").contains("500").doesNotContain("077092209");
    }

    @Test
    void anAnswerIsCutToALogFriendlyLength() {
        assertThat(PvitAnswer.masked("x".repeat(2000))).hasSize(500);
    }

    @Test
    void anEmptyAnswerIsEmpty() {
        assertThat(PvitAnswer.masked(null)).isEmpty();
    }
}
