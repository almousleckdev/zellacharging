package com.evcharge.customer.admin.application;

import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;

import java.util.List;

final class AdminCountries {

    private AdminCountries() {
    }

    static String single(List<String> scope) {
        if (scope.size() != 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "COUNTRY_REQUIRED", "A single country is required");
        }
        return scope.getFirst();
    }
}
