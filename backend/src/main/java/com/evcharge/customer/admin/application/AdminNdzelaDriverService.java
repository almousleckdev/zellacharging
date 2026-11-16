package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.partner.NdzelaDriver;
import com.evcharge.customer.partner.NdzelaDrivers;
import com.evcharge.customer.partner.PartnerLinking;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminNdzelaDriverService {

    private final AdminAccessService access;
    private final PartnerLinking linking;
    private final NdzelaDrivers drivers;

    public AdminNdzelaDriverService(AdminAccessService access, PartnerLinking linking, NdzelaDrivers drivers) {
        this.access = access;
        this.linking = linking;
        this.drivers = drivers;
    }

    public NdzelaDriver driver(AdminAccess admin, String country, UUID customerId) {
        String code = AdminCountries.single(access.scope(admin, Permission.CUSTOMERS_READ, country));
        if (!TenantContext.callAs(code, () -> linking.linked(customerId))) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NDZELA_NOT_LINKED", "The customer is not linked to NDZela");
        }
        return drivers.find(customerId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                "NDZELA_DRIVER_NOT_FOUND", "NDZela has no driver for this customer"));
    }
}
