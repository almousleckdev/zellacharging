package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.partner.AgreementTerms;
import com.evcharge.customer.partner.FreeAgreement;
import com.evcharge.customer.partner.FreeChargingAdministration;
import com.evcharge.customer.partner.FreeChargingUsePage;
import com.evcharge.customer.partner.PartnerLinking;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.CountryDirectory;
import com.evcharge.customer.shared.FreeSundayWindow;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
public class AdminFreeChargingService {

    private static final int MAXIMUM_PAGE_SIZE = 100;

    private final AdminAccessService access;
    private final FreeChargingAdministration ndzela;
    private final AdminAudit audit;
    private final CountryDirectory countries;
    private final PartnerLinking linking;

    public AdminFreeChargingService(AdminAccessService access, FreeChargingAdministration ndzela, AdminAudit audit,
                                    CountryDirectory countries, PartnerLinking linking) {
        this.access = access;
        this.ndzela = ndzela;
        this.audit = audit;
        this.countries = countries;
        this.linking = linking;
    }

    public List<FreeAgreement> list(AdminAccess admin) {
        authorize(admin, Permission.FREE_CHARGING_READ);
        return ndzela.list(FreeChargingActor.of(admin));
    }

    public FreeSundayWindow window(AdminAccess admin) {
        authorize(admin, Permission.FREE_CHARGING_READ);
        return ndzela.window(FreeChargingActor.of(admin));
    }

    public FreeSundayWindow changeWindow(AdminAccess admin, FreeSundayWindow window) {
        String country = authorize(admin, Permission.FREE_CHARGING_MANAGE);
        FreeSundayWindow saved = ndzela.changeWindow(FreeChargingActor.of(admin), window);
        audit.record(admin.id(), country, "FREE_WINDOW_CHANGED", "window",
                saved.day() + " " + saved.from() + "-" + saved.until());
        return saved;
    }

    public FreeChargingUsePage usage(AdminAccess admin, int page, int size) {
        authorize(admin, Permission.FREE_CHARGING_READ);
        if (page < 0 || size < 1 || size > MAXIMUM_PAGE_SIZE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGING", "Page or size out of range");
        }
        return ndzela.usage(FreeChargingActor.of(admin), page, size);
    }

    public FreeAgreement create(AdminAccess admin, UUID customerId, AgreementTerms terms) {
        String country = authorize(admin, Permission.FREE_CHARGING_MANAGE);
        requireLinked(country, customerId);
        return perform(admin, country, "FREE_AGREEMENT_CREATED", "customer " + customerId,
                actor -> ndzela.create(actor, customerId, terms));
    }

    public FreeAgreement update(AdminAccess admin, UUID agreementId, AgreementTerms terms) {
        return mutate(admin, "FREE_AGREEMENT_UPDATED", null, actor -> ndzela.update(actor, agreementId, terms));
    }

    public FreeAgreement suspend(AdminAccess admin, UUID agreementId) {
        return mutate(admin, "FREE_AGREEMENT_SUSPENDED", null, actor -> ndzela.suspend(actor, agreementId));
    }

    public FreeAgreement resume(AdminAccess admin, UUID agreementId) {
        return mutate(admin, "FREE_AGREEMENT_RESUMED", null, actor -> ndzela.resume(actor, agreementId));
    }

    public FreeAgreement revoke(AdminAccess admin, UUID agreementId) {
        return mutate(admin, "FREE_AGREEMENT_REVOKED", null, actor -> ndzela.revoke(actor, agreementId));
    }

    private FreeAgreement mutate(AdminAccess admin, String action, String detail,
                                 Function<String, FreeAgreement> call) {
        return perform(admin, authorize(admin, Permission.FREE_CHARGING_MANAGE), action, detail, call);
    }

    private FreeAgreement perform(AdminAccess admin, String country, String action, String detail,
                                  Function<String, FreeAgreement> call) {
        FreeAgreement agreement = call.apply(FreeChargingActor.of(admin));
        audit.record(admin.id(), country, action, agreement.id().toString(), detail);
        return agreement;
    }

    private void requireLinked(String country, UUID customerId) {
        if (!TenantContext.callAs(country, () -> linking.linked(customerId))) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "FREE_AGREEMENT_NOT_LINKED",
                    "The customer is not linked to NDZela");
        }
    }

    private String authorize(AdminAccess admin, Permission permission) {
        return AdminCountries.single(access.scope(admin, permission, countries.defaultCountry().code()));
    }
}
