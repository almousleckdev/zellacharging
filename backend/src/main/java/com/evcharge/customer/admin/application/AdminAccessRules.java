package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.CountryCatalog;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.Country;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
class AdminAccessRules {

    private final CountryCatalog catalog;

    AdminAccessRules(CountryCatalog catalog) {
        this.catalog = catalog;
    }

    AccessCommand validated(AccessCommand command) {
        if (command.role() == null || command.role() == AdminRole.SUPER_ADMIN) {
            throw invalid("INVALID_ROLE");
        }
        Set<Permission> permissions = command.permissions() == null || command.permissions().isEmpty()
                ? EnumSet.copyOf(command.role().permissions()) : EnumSet.copyOf(command.permissions());
        if (permissions.contains(Permission.ADMINS_MANAGE)) {
            throw invalid("INVALID_PERMISSIONS");
        }
        Set<String> known = catalog.all().stream().map(Country::code).collect(Collectors.toSet());
        if (command.countries() == null || command.countries().isEmpty() || !known.containsAll(command.countries())) {
            throw invalid("INVALID_COUNTRIES");
        }
        return new AccessCommand(command.role(), permissions, Set.copyOf(command.countries()));
    }

    private static ApiException invalid(String code) {
        return new ApiException(HttpStatus.BAD_REQUEST, code, "Invalid access definition");
    }
}
