package com.evcharge.customer.wallet.infrastructure;

import com.evcharge.customer.wallet.application.PaymentSettings;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
class PaymentProviderGuard implements ApplicationRunner {

    private final PaymentSettings settings;

    PaymentProviderGuard(PaymentSettings settings) {
        this.settings = settings;
    }

    @Override
    public void run(ApplicationArguments args) {
        if ("sandbox".equals(settings.provider()) && !settings.allowSandbox()) {
            throw new IllegalStateException("The sandbox payment provider credits money without collecting it: "
                    + "set zella.payment.allow-sandbox=true only in a non-production environment");
        }
    }
}
