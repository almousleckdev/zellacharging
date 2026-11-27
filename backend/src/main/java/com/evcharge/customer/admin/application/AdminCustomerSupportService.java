package com.evcharge.customer.admin.application;

import com.evcharge.customer.account.CustomerCreation;
import com.evcharge.customer.account.CustomerSupport;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.partner.PartnerLinking;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Service
public class AdminCustomerSupportService {

    private final AdminAccessService access;
    private final CustomerSupport support;
    private final CustomerCreation creation;
    private final PartnerLinking linking;
    private final AdminAudit audit;

    public AdminCustomerSupportService(AdminAccessService access, CustomerSupport support,
                                       CustomerCreation creation, PartnerLinking linking, AdminAudit audit) {
        this.access = access;
        this.support = support;
        this.creation = creation;
        this.linking = linking;
        this.audit = audit;
    }

    public UUID createCustomer(AdminAccess admin, String country, String firstName, String lastName, String email,
                               String phone) {
        String code = AdminCountries.single(access.scope(admin, Permission.CUSTOMERS_MANAGE, country));
        UUID id = TenantContext.callAs(code, () -> creation.create(firstName, lastName, email, phone));
        audit.record(admin.id(), code, "CUSTOMER_CREATED", id.toString(), null);
        return id;
    }

    public void resendCode(AdminAccess admin, String country, UUID id, String channel, String delivery) {
        act(admin, country, id, "CUSTOMER_CODE_RESENT", "channel=" + channel,
                () -> support.resendCode(id, channel, delivery));
    }

    public void resetCodeAttempts(AdminAccess admin, String country, UUID id, String channel) {
        act(admin, country, id, "CUSTOMER_CODE_ATTEMPTS_RESET", "channel=" + channel,
                () -> support.resetCodeAttempts(id, channel));
    }

    public void unlockLogin(AdminAccess admin, String country, UUID id) {
        act(admin, country, id, "CUSTOMER_UNLOCKED", null, () -> support.unlockLogin(id));
    }

    public void markVerified(AdminAccess admin, String country, UUID id, String channel) {
        act(admin, country, id, "CUSTOMER_CONTACT_VERIFIED", "channel=" + channel,
                () -> support.markVerified(id, channel));
    }

    public void changeName(AdminAccess admin, String country, UUID id, String firstName, String lastName) {
        requireRole(admin, EnumSet.of(AdminRole.SUPER_ADMIN));
        act(admin, country, id, "CUSTOMER_NAME_CHANGED", null, () -> support.changeName(id, firstName, lastName));
    }

    public void setPassword(AdminAccess admin, String country, UUID id, String password) {
        requireRole(admin, EnumSet.of(AdminRole.SUPER_ADMIN, AdminRole.ADMIN));
        act(admin, country, id, "CUSTOMER_PASSWORD_CHANGED", null, () -> support.setPassword(id, password));
    }

    public void changeContact(AdminAccess admin, String country, UUID id, String channel, String value) {
        act(admin, country, id, "CUSTOMER_CONTACT_CHANGED", "channel=" + channel,
                () -> support.changeContact(id, channel, value));
    }

    public void linkNdzela(AdminAccess admin, String country, UUID id) {
        act(admin, country, id, "CUSTOMER_NDZELA_LINKED", null, () -> linking.link(id, admin.id()));
    }

    public void unlinkNdzela(AdminAccess admin, String country, UUID id) {
        act(admin, country, id, "CUSTOMER_NDZELA_UNLINKED", null, () -> linking.unlink(id, admin.id()));
    }

    private static void requireRole(AdminAccess admin, Set<AdminRole> allowed) {
        if (!allowed.contains(admin.role())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "PERMISSION_DENIED", "Your role cannot do this");
        }
    }

    private void act(AdminAccess admin, String country, UUID id, String action, String detail, Runnable work) {
        String code = AdminCountries.single(access.scope(admin, Permission.CUSTOMERS_MANAGE, country));
        TenantContext.runAs(code, work);
        audit.record(admin.id(), code, action, id.toString(), detail);
    }
}
