package com.evcharge.customer.catalog;

import com.evcharge.customer.catalog.domain.QrContent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QrContentTest {

    @Test
    void extractsTokenFromUrlOrPlainValue() {
        assertThat(QrContent.tokenOf("https://h.example/c/abc.def?x=1")).isEqualTo("abc.def");
        assertThat(QrContent.tokenOf("  abc.def ")).isEqualTo("abc.def");
        assertThat(QrContent.tokenOf("https://h.example/c/")).isEmpty();
    }

    @Test
    void keepsShortTokenIntact() {
        String token = "Ab3-_xYz09QrStUvWxYz12";

        assertThat(token).hasSize(22);
        assertThat(QrContent.tokenOf("https://zella-ocpp.com/c/" + token)).isEqualTo(token);
    }

    @Test
    void keepsLongSignedTokenIntact() {
        String token = "UkZreU1ESTJNRGt4TVRBd01EZ3wxfDE4NTI0NDUzMzI." + "A".repeat(43);

        assertThat(token).hasSize(87);
        assertThat(QrContent.tokenOf("https://zella-ocpp.com/c/" + token)).isEqualTo(token);
    }
}
