package com.evcharge.customer.shared;

import com.evcharge.customer.PostgresTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "ZELLA_APP_STORE_URL=https://apps.apple.com/app/id1234567890")
class AppDownloadIntegrationTest extends PostgresTestBase {

    private static final String PLAY = "https://play.google.com/store/apps/details?id=com.zella.charging.gabon";

    @Autowired
    MockMvc mvc;

    @Test
    void anIphoneGoesToTheAppStore() throws Exception {
        mvc.perform(get("/v1/app/download").header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0)"))
                .andExpect(status().isTemporaryRedirect())
                .andExpect(header().string("Location", "https://apps.apple.com/app/id1234567890"));
    }

    @Test
    void anAndroidPhoneGoesToGooglePlay() throws Exception {
        mvc.perform(get("/v1/app/download").header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 8)"))
                .andExpect(status().isTemporaryRedirect()).andExpect(header().string("Location", PLAY));
    }

    @Test
    void anythingElseGoesToGooglePlayToo() throws Exception {
        mvc.perform(get("/v1/app/download")).andExpect(status().isTemporaryRedirect())
                .andExpect(header().string("Location", PLAY));
    }
}
