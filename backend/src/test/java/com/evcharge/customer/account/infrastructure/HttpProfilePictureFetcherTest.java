package com.evcharge.customer.account.infrastructure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class HttpProfilePictureFetcherTest {

    @Test
    void aGooglePictureAddressIsTrusted() {
        assertThat(HttpProfilePictureFetcher.trusted("https://lh3.googleusercontent.com/a/photo=s96")).isPresent();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://lh3.googleusercontent.com/a/photo",
            "https://evil.example.com/a.png",
            "https://googleusercontent.com.evil.example.com/a.png",
            "https://user@lh3.googleusercontent.com/a.png",
            "http://169.254.169.254/latest/meta-data",
            "not a url"})
    void anyOtherAddressIsRefused(String url) {
        assertThat(HttpProfilePictureFetcher.trusted(url)).isEmpty();
    }
}
