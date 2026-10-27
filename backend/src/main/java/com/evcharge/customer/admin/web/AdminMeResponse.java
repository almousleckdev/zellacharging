package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.ManagedAdmin;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.shared.Country;

import java.util.List;
import java.util.UUID;

record AdminMeResponse(UUID id, String email, String firstName, String lastName, String phone, String role,
                       List<String> permissions, List<CountryView> countries, boolean profileComplete,
                       Long avatarVersion) {

    static AdminMeResponse of(AdminAccess access, List<Country> countries, ManagedAdmin current) {
        var user = current.user();
        return new AdminMeResponse(access.id(), access.email(), user.firstName(), user.lastName(), user.phone(),
                access.role().name(), access.permissions().stream().map(Enum::name).sorted().toList(),
                countries.stream().map(CountryView::from).toList(), access.profileComplete(),
                current.avatarUpdatedAt() == null ? null : current.avatarUpdatedAt().toEpochMilli());
    }

    record CountryView(String code, String name, String currency, int currencyDecimals) {

        static CountryView from(Country country) {
            return new CountryView(country.code(), country.name(), country.currency(), country.currencyDecimals());
        }
    }
}
