package com.evcharge.customer.partner.infrastructure;

import com.evcharge.customer.shared.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Set;
import java.util.regex.Pattern;

final class NdzelaFailure {

    private static final Logger log = LoggerFactory.getLogger(NdzelaFailure.class);
    private static final Set<Integer> PASSED_THROUGH = Set.of(400, 404, 409, 422);
    private static final Pattern STABLE_CODE = Pattern.compile("[A-Z][A-Z0-9_]{2,63}");
    private static final String UNAVAILABLE = "FREE_CHARGING_UNAVAILABLE";

    private NdzelaFailure() {
    }

    static ApiException unavailable() {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, UNAVAILABLE, "Free charging management is unavailable");
    }

    static String describe(RuntimeException failure) {
        String kind = failure.getClass().getSimpleName();
        return failure instanceof RestClientResponseException refused
                ? kind + " status " + refused.getStatusCode().value() : kind;
    }

    static ApiException of(RuntimeException failure, boolean agreementRoute) {
        if (failure instanceof ApiException api) {
            return api;
        }
        if (failure instanceof RestClientResponseException refused) {
            return refusal(refused, agreementRoute);
        }
        if (failure instanceof RestClientException) {
            log.warn("NDZela could not be reached for free charging management");
        } else {
            log.warn("NDZela gave an unusable free charging answer");
        }
        return unavailable();
    }

    private static ApiException refusal(RestClientResponseException refused, boolean agreementRoute) {
        int status = refused.getStatusCode().value();
        boolean passable = PASSED_THROUGH.contains(status) && (status != 404 || agreementRoute);
        String code = passable ? codeOf(refused) : null;
        log.warn("NDZela refused a free charging call with status {} and code {}", status, code);
        if (code == null) {
            return unavailable();
        }
        return new ApiException(HttpStatus.valueOf(status), code, "NDZela refused the request");
    }

    private static String codeOf(RestClientResponseException refused) {
        try {
            NdzelaProblem problem = refused.getResponseBodyAs(NdzelaProblem.class);
            return problem != null && problem.code() != null && STABLE_CODE.matcher(problem.code()).matches()
                    ? problem.code() : null;
        } catch (RuntimeException unreadable) {
            return null;
        }
    }
}
