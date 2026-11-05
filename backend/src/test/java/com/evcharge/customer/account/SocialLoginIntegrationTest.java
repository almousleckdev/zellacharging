package com.evcharge.customer.account;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
import com.evcharge.customer.account.application.port.AppleTokenRevoker;
import com.evcharge.customer.account.application.port.PasswordHasher;
import com.evcharge.customer.account.application.port.ProfilePictureFetcher;
import com.evcharge.customer.account.application.port.SocialTokenVerifier;
import com.evcharge.customer.account.domain.Avatar;
import com.evcharge.customer.account.domain.SocialProfile;
import com.evcharge.customer.account.domain.SocialProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SocialLoginIntegrationTest extends PostgresTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PasswordHasher hasher;

    @MockitoBean
    SocialTokenVerifier verifier;

    @MockitoBean
    AppleTokenRevoker revoker;

    @MockitoBean
    ProfilePictureFetcher pictures;

    private static final String PICTURE = "https://lh3.googleusercontent.com/a/photo";

    private String email() {
        return "social-" + UUID.randomUUID() + "@example.com";
    }

    private void token(String token, SocialProvider provider, String subject, String email, boolean verified) {
        when(verifier.verify(provider, token))
                .thenReturn(new SocialProfile(provider, subject, email, verified, "Aline", "Obame", PICTURE));
    }

    private ResultActions signIn(SocialProvider provider, String token) throws Exception {
        return mvc.perform(post("/v1/auth/social").contentType(MediaType.APPLICATION_JSON)
                .content("{\"provider\":\"%s\",\"idToken\":\"%s\"}".formatted(provider, token)));
    }

    private UUID customerOf(String email) {
        return jdbc.queryForObject("select id from customer where email = ?", UUID.class, email);
    }

    @Test
    void aNewGoogleUserGetsAVerifiedPasswordlessAccount() throws Exception {
        String email = email();
        token("g1", SocialProvider.GOOGLE, "sub-" + email, email, true);

        signIn(SocialProvider.GOOGLE, "g1").andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").exists());

        UUID id = customerOf(email);
        assertThat(jdbc.queryForObject("select status from customer where id = ?", String.class, id))
                .isEqualTo("ACTIVE");
        assertThat(jdbc.queryForObject("select email_verified_at is not null from customer where id = ?",
                Boolean.class, id)).isTrue();
        assertThat(jdbc.queryForObject("select password_login from customer where id = ?", Boolean.class, id))
                .isFalse();
    }

    @Test
    void aNewGoogleUserGetsTheirGooglePictureAsAvatar() throws Exception {
        String email = email();
        when(pictures.fetch(PICTURE)).thenReturn(Optional.of(new Avatar("image/jpeg", new byte[]{1, 2, 3})));
        token("g3", SocialProvider.GOOGLE, "sub-" + email, email, true);

        signIn(SocialProvider.GOOGLE, "g3").andExpect(status().isOk());

        assertThat(jdbc.queryForObject("select count(*) from customer_avatar where customer_id = ?",
                Integer.class, customerOf(email))).isEqualTo(1);
    }

    @Test
    void aPictureAlreadyChosenIsNeverReplacedByTheGoogleOne() throws Exception {
        String email = email();
        token("g4", SocialProvider.GOOGLE, "sub-" + email, email, true);
        signIn(SocialProvider.GOOGLE, "g4").andExpect(status().isOk());
        jdbc.update("insert into customer_avatar (customer_id, content_type, data, updated_at) "
                + "values (?, 'image/png', ?, now())", customerOf(email), new byte[]{9});
        when(pictures.fetch(PICTURE)).thenReturn(Optional.of(new Avatar("image/jpeg", new byte[]{1, 2, 3})));

        signIn(SocialProvider.GOOGLE, "g4").andExpect(status().isOk());

        assertThat(jdbc.queryForObject("select content_type from customer_avatar where customer_id = ?",
                String.class, customerOf(email))).isEqualTo("image/png");
    }

    @Test
    void theSameProviderAccountAlwaysReachesTheSameCustomer() throws Exception {
        String email = email();
        token("g2", SocialProvider.GOOGLE, "sub-" + email, email, true);

        signIn(SocialProvider.GOOGLE, "g2").andExpect(status().isOk());
        signIn(SocialProvider.GOOGLE, "g2").andExpect(status().isOk());

        assertThat(jdbc.queryForObject("select count(*) from customer where email = ?", Integer.class, email))
                .isEqualTo(1);
    }

    @Test
    void aVerifiedEmailAccountIsLinkedAndKeepsItsPassword() throws Exception {
        String email = email();
        UUID id = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, email_verified_at, password_hash, "
                + "status, created_at) values (?, 'A', 'B', ?, now(), ?, 'ACTIVE', now())", id, email,
                hasher.hash("Secret123!"));
        token("a1", SocialProvider.APPLE, "apple-" + email, email, true);

        signIn(SocialProvider.APPLE, "a1").andExpect(status().isOk());

        assertThat(jdbc.queryForObject("select count(*) from customer where email = ?", Integer.class, email))
                .isEqualTo(1);
        assertThat(jdbc.queryForObject("select password_login from customer where id = ?", Boolean.class, id))
                .isTrue();
        assertThat(jdbc.queryForObject("select count(*) from customer_identity where customer_id = ?",
                Integer.class, id)).isEqualTo(1);
    }

    @Test
    void anUnverifiedRegistrationCannotBeHijackedThroughALaterSocialSignIn() throws Exception {
        String email = email();
        UUID id = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, password_hash, status, created_at) "
                + "values (?, 'A', 'B', ?, ?, 'PENDING_VERIFICATION', now())", id, email, hasher.hash("Attacker1!"));
        token("g3", SocialProvider.GOOGLE, "sub-" + email, email, true);

        signIn(SocialProvider.GOOGLE, "g3").andExpect(status().isOk());

        assertThat(jdbc.queryForObject("select password_login from customer where id = ?", Boolean.class, id))
                .isFalse();
        assertThat(hasher.matches("Attacker1!",
                jdbc.queryForObject("select password_hash from customer where id = ?", String.class, id))).isFalse();
    }

    @Test
    void anUnverifiedProviderEmailIsRefused() throws Exception {
        String email = email();
        token("g4", SocialProvider.GOOGLE, "sub-" + email, email, false);

        signIn(SocialProvider.GOOGLE, "g4").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SOCIAL_EMAIL_UNVERIFIED"));
    }

    @Test
    void aSuspendedAccountCannotSignIn() throws Exception {
        String email = email();
        token("g5", SocialProvider.GOOGLE, "sub-" + email, email, true);
        signIn(SocialProvider.GOOGLE, "g5").andExpect(status().isOk());
        jdbc.update("update customer set status = 'BLOCKED' where email = ?", email);

        signIn(SocialProvider.GOOGLE, "g5").andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_SUSPENDED"));
    }

    @Test
    void aPasswordlessAccountIsDeletedWithItsProviderProofAndAppleIsRevoked() throws Exception {
        String email = email();
        token("a2", SocialProvider.APPLE, "apple-" + email, email, true);
        signIn(SocialProvider.APPLE, "a2").andExpect(status().isOk());
        UUID id = customerOf(email);
        jdbc.update("insert into wallet (customer_id, currency, decimals, balance_minor, created_at) "
                + "values (?, 'XOF', 0, 0, now())", id);

        mvc.perform(get("/v1/me").with(TestTenants.customer(id))).andExpect(jsonPath("$.hasPassword").value(false));
        mvc.perform(post("/v1/me/delete").with(TestTenants.customer(id)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"anything\"}")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SOCIAL_PROOF_REQUIRED"));

        token("other", SocialProvider.APPLE, "apple-someone-else", email(), true);
        mvc.perform(post("/v1/me/delete").with(TestTenants.customer(id)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"provider\":\"APPLE\",\"idToken\":\"other\"}")).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("SOCIAL_IDENTITY_MISMATCH"));

        mvc.perform(post("/v1/me/delete").with(TestTenants.customer(id)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"provider\":\"APPLE\",\"idToken\":\"a2\",\"authorizationCode\":\"code-1\"}"))
                .andExpect(status().isNoContent());

        verify(revoker).revoke("code-1");
        assertThat(jdbc.queryForObject("select count(*) from customer_identity where customer_id = ?",
                Integer.class, id)).isZero();
        assertThat(jdbc.queryForObject("select status from customer where id = ?", String.class, id))
                .isEqualTo("DELETED");
    }
}
