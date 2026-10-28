package com.evcharge.customer.wallet.infrastructure.pvit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class PvitConfigurationTest {

    private static final String[] COMMON = {
            "zella.payment.provider=pvit",
            "zella.payment.pvit.base-url=http://pvit.test",
            "zella.payment.pvit.secret-password=pw",
            "zella.payment.pvit.code-url-secret=S",
            "zella.payment.pvit.code-url-payment=P",
            "zella.payment.pvit.code-url-status=T",
            "zella.payment.pvit.callback-url-code=C",
            "zella.payment.pvit.agent=A",
            "zella.payment.pvit.product=PR",
            "zella.payment.pvit.owner-charge=CUSTOMER",
            "zella.payment.pvit.request-timeout=5s"};

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(PvitConfiguration.class)
            .withBean(ObjectMapper.class, () -> JsonMapper.builder().build())
            .withPropertyValues(COMMON);

    @Test
    void onlyTheConfiguredOperatorGetsAGateway() {
        runner.withPropertyValues("zella.payment.pvit.compte-operation-airtel=ACC-A").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasBean("pvitAirtelGateway").doesNotHaveBean("pvitMoovGateway");
            assertThat(context).doesNotHaveBean(PvitPayoutGateway.class);
        });
    }

    @Test
    void bothOperatorsGetTheirOwnGatewayOnceTheirAccountsAreSet() {
        runner.withPropertyValues("zella.payment.pvit.compte-operation-airtel=ACC-A",
                "zella.payment.pvit.compte-operation-moov=ACC-M").run(context ->
                assertThat(context).hasBean("pvitAirtelGateway").hasBean("pvitMoovGateway"));
    }

    @Test
    void thePayoutGatewayNeedsItsCodeAndTheOperatorAccount() {
        runner.withPropertyValues("zella.payment.pvit.compte-operation-moov=ACC-M",
                "zella.payment.pvit.code-url-payout=O").run(context -> {
            assertThat(context).hasBean("pvitMoovPayoutGateway").doesNotHaveBean("pvitAirtelPayoutGateway");
        });
    }

    @Test
    void withoutAnyOperatorAccountTheServiceRefusesToStart() {
        runner.run(context -> assertThat(context.getStartupFailure())
                .hasRootCauseMessage("Payment provider pvit is selected but these settings are missing: "
                        + "PVIT_COMPTE_OPERATION_AIRTEL or PVIT_COMPTE_OPERATION_MOOV"));
    }

    @Test
    void aMissingSharedSettingIsNamed() {
        new ApplicationContextRunner().withUserConfiguration(PvitConfiguration.class)
                .withBean(ObjectMapper.class, () -> JsonMapper.builder().build())
                .withPropertyValues("zella.payment.provider=pvit", "zella.payment.pvit.base-url=http://pvit.test",
                        "zella.payment.pvit.compte-operation-airtel=ACC-A",
                        "zella.payment.pvit.request-timeout=5s")
                .run(context -> assertThat(context.getStartupFailure())
                        .rootCause().hasMessageContaining("PVIT_SECRET_PASSWORD")
                        .hasMessageContaining("PVIT_AGENT"));
    }
}
