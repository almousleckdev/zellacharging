package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.application.port.CountryCatalog;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.AdminUser;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.Country;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Service
public class AdminAccessService {

    public static final String ALL_COUNTRIES = "ALL";

    private final AdminRepository admins;
    private final CountryCatalog catalog;

    public AdminAccessService(AdminRepository admins, CountryCatalog catalog) {
        this.admins = admins;
        this.catalog = catalog;
    }

    public AdminAccess load(UUID adminId) {
        AdminUser admin = admins.findById(adminId).filter(AdminUser::active)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED",
                        "Authentication required"));
        Set<String> countries = new TreeSet<>();
        for (Country country : catalog.all()) {
            if (admin.role().spansAllCountries() || admin.grantedCountries().contains(country.code())) {
                countries.add(country.code());
            }
        }
        return new AdminAccess(admin.id(), admin.email(), admin.role(), countries, admin.effectivePermissions(),
                admin.profileComplete());
    }

    public List<Country> visibleCountries(AdminAccess access) {
        return catalog.all().stream().filter(country -> access.canSee(country.code())).toList();
    }

    public void require(AdminAccess access, Permission permission) {
        if (!access.profileComplete()) {
            throw forbidden("PROFILE_INCOMPLETE");
        }
        if (!access.can(permission)) {
            throw forbidden("PERMISSION_DENIED");
        }
    }

    public List<String> scope(AdminAccess access, Permission permission, String requested) {
        require(access, permission);
        if (requested == null || requested.isBlank() || ALL_COUNTRIES.equalsIgnoreCase(requested)) {
            if (access.countries().isEmpty()) {
                throw forbidden("COUNTRY_FORBIDDEN");
            }
            return List.copyOf(access.countries());
        }
        String code = requested.trim().toUpperCase(Locale.ROOT);
        if (!access.canSee(code)) {
            throw forbidden("COUNTRY_FORBIDDEN");
        }
        return List.of(code);
    }

    private static ApiException forbidden(String code) {
        return new ApiException(HttpStatus.FORBIDDEN, code, "Forbidden");
    }
}
