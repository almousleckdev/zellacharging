package com.evcharge.customer.wallet;

import com.evcharge.customer.wallet.domain.TopupReference;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TopupReferenceTest {

    @Test
    void referencesFitTheProvidersPaymentLimitOfTwentyCharacters() {
        assertThat(TopupReference.MAX_LENGTH).isEqualTo(20);
        for (int sample = 0; sample < 2000; sample++) {
            assertThat(TopupReference.forPayment("GA")).hasSizeLessThanOrEqualTo(20).matches("[A-Z0-9]+");
            assertThat(TopupReference.forWithdrawal("GA")).hasSizeLessThanOrEqualTo(20).matches("[A-Z0-9]+");
        }
    }

    @Test
    void referencesAreUniqueInPractice() {
        Set<String> seen = new HashSet<>();
        for (int sample = 0; sample < 20000; sample++) {
            assertThat(seen.add(TopupReference.forPayment("GA"))).isTrue();
        }
    }

    @Test
    void theCountryCanBeReadBackButGarbageIsRejected() {
        assertThat(TopupReference.countryOf(TopupReference.forPayment("ML"))).contains("ML");
        assertThat(TopupReference.countryOf("ML" + "0".repeat(18))).isEmpty();
        assertThat(TopupReference.countryOf("GA" + "A".repeat(26))).isEmpty();
        assertThat(TopupReference.countryOf(null)).isEmpty();
        assertThat(TopupReference.countryOf("short")).isEmpty();
    }
}
