package com.evcharge.customer.partner;

import com.evcharge.customer.shared.FreeSundayWindow;

import java.util.List;
import java.util.UUID;

public interface FreeChargingAdministration {

    List<FreeAgreement> list(String actor);

    FreeAgreement create(String actor, UUID customerId, AgreementTerms terms);

    FreeAgreement update(String actor, UUID agreementId, AgreementTerms terms);

    FreeAgreement suspend(String actor, UUID agreementId);

    FreeAgreement resume(String actor, UUID agreementId);

    FreeAgreement revoke(String actor, UUID agreementId);

    FreeSundayWindow window(String actor);

    FreeSundayWindow changeWindow(String actor, FreeSundayWindow window);

    FreeChargingUsePage usage(String actor, int page, int size);
}
