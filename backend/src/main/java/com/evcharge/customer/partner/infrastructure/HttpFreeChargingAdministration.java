package com.evcharge.customer.partner.infrastructure;

import com.evcharge.customer.partner.AgreementTerms;
import com.evcharge.customer.partner.FreeAgreement;
import com.evcharge.customer.partner.FreeChargingAdministration;
import com.evcharge.customer.partner.FreeChargingUsePage;
import com.evcharge.customer.shared.FreeSundayWindow;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Component
class HttpFreeChargingAdministration implements FreeChargingAdministration {

    private static final String AGREEMENTS = "/agreements";
    private static final String WINDOW = "/window";

    private final NdzelaRestClient ndzela;

    HttpFreeChargingAdministration(NdzelaRestClient ndzela) {
        this.ndzela = ndzela;
    }

    @Override
    public List<FreeAgreement> list(String actor) {
        return answer(false, () -> ndzela.manage(HttpMethod.GET, AGREEMENTS, actor, null)
                .body(new ParameterizedTypeReference<List<FreeAgreement>>() {
                }));
    }

    @Override
    public FreeAgreement create(String actor, UUID customerId, AgreementTerms terms) {
        return agreement(HttpMethod.POST, AGREEMENTS, actor, new NdzelaAgreementBody(customerId, terms.startsOn(),
                terms.endsOn(), terms.conditions()));
    }

    @Override
    public FreeAgreement update(String actor, UUID agreementId, AgreementTerms terms) {
        return agreement(HttpMethod.PUT, AGREEMENTS + "/" + agreementId, actor,
                new NdzelaAgreementBody(null, terms.startsOn(), terms.endsOn(), terms.conditions()));
    }

    @Override
    public FreeAgreement suspend(String actor, UUID agreementId) {
        return agreement(HttpMethod.POST, AGREEMENTS + "/" + agreementId + "/suspend", actor, null);
    }

    @Override
    public FreeAgreement resume(String actor, UUID agreementId) {
        return agreement(HttpMethod.POST, AGREEMENTS + "/" + agreementId + "/resume", actor, null);
    }

    @Override
    public FreeAgreement revoke(String actor, UUID agreementId) {
        return agreement(HttpMethod.POST, AGREEMENTS + "/" + agreementId + "/revoke", actor, null);
    }

    @Override
    public FreeSundayWindow window(String actor) {
        return answer(false, () -> ndzela.manage(HttpMethod.GET, WINDOW, actor, null).body(FreeSundayWindow.class));
    }

    @Override
    public FreeSundayWindow changeWindow(String actor, FreeSundayWindow window) {
        return answer(false, () -> ndzela.manage(HttpMethod.PUT, WINDOW, actor, window).body(FreeSundayWindow.class));
    }

    @Override
    public FreeChargingUsePage usage(String actor, int page, int size) {
        return answer(false, () -> ndzela.manage(HttpMethod.GET, "/usage?page=" + page + "&size=" + size, actor, null)
                .body(FreeChargingUsePage.class));
    }

    private FreeAgreement agreement(HttpMethod method, String path, String actor, Object body) {
        return answer(path.length() > AGREEMENTS.length(), () -> ndzela.manage(method, path, actor, body).body(FreeAgreement.class));
    }

    private static <T> T answer(boolean agreementRoute, Supplier<T> call) {
        try {
            T result = call.get();
            if (result == null) {
                throw NdzelaFailure.unavailable();
            }
            return result;
        } catch (RuntimeException e) {
            throw NdzelaFailure.of(e, agreementRoute);
        }
    }
}
