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
}
