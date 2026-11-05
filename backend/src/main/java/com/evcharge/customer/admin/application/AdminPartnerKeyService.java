package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.partner.IssuedKey;
import com.evcharge.customer.partner.PartnerKeyInfo;
import com.evcharge.customer.partner.PartnerKeys;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.CountryDirectory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AdminPartnerKeyService {

    private static final String NDZELA = "NDZELA";

    private final AdminAccessService access;
    private final PartnerKeys keys;
    private final CountryDirectory countries;

    public AdminPartnerKeyService(AdminAccessService access, PartnerKeys keys, CountryDirectory countries) {
        this.access = access;
        this.keys = keys;
        this.countries = countries;
    }

    public IssuedKey issue(AdminAccess admin, String partner, String country) {
        authorize(admin, partner);
        if (countries.activeByCode(country).isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "COUNTRY_NOT_FOUND", "Country not found");
        }
        return keys.issue(partner, country, admin.id());
    }

    public void revoke(AdminAccess admin, String partner, UUID keyId) {
        authorize(admin, partner);
        keys.revoke(keyId, admin.id());
    }

    public List<PartnerKeyInfo> list(AdminAccess admin, String partner) {
        authorize(admin, partner);
        return keys.list(partner);
    }

    private void authorize(AdminAccess admin, String partner) {
        access.require(admin, Permission.ADMINS_MANAGE);
        if (!NDZELA.equals(partner)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "PARTNER_NOT_FOUND", "Partner not found");
        }
    }
}
