package com.evcharge.customer.partner.web;

import com.evcharge.customer.partner.application.PartnerKeyService;
import com.evcharge.customer.partner.application.PartnerPrincipal;
import com.evcharge.customer.partner.application.PartnerSettings;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.CountryDirectory;
import com.evcharge.customer.shared.RateLimiter;
import com.evcharge.customer.shared.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

class PartnerKeyFilter extends OncePerRequestFilter {

    static final String HEADER = "X-Api-Key";
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final PartnerKeyService keys;
    private final CountryDirectory countries;
    private final RateLimiter limiter;
    private final PartnerSettings settings;
    private final HandlerExceptionResolver resolver;

    PartnerKeyFilter(PartnerKeyService keys, CountryDirectory countries, RateLimiter limiter,
                     PartnerSettings settings, HandlerExceptionResolver resolver) {
        this.keys = keys;
        this.countries = countries;
        this.limiter = limiter;
        this.settings = settings;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Optional<PartnerPrincipal> principal = keys.authenticate(request.getHeader(HEADER))
                .filter(found -> countries.activeByCode(found.country()).isPresent());
        if (principal.isEmpty()) {
            reject(request, response, new ApiException(HttpStatus.UNAUTHORIZED, "PARTNER_KEY_INVALID",
                    "Invalid or missing API key"));
            return;
        }
        PartnerPrincipal partner = principal.get();
        if (!limiter.tryAcquire("partner", partner.keyId().toString(), settings.ratePerMinute(), WINDOW)) {
            response.setHeader("Retry-After", Long.toString(WINDOW.toSeconds()));
            reject(request, response, new ApiException(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS",
                    "Too many requests"));
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(new KeyAuthentication(partner));
        try (TenantContext.Scope scope = TenantContext.open(partner.country())) {
            chain.doFilter(request, response);
        }
    }

    private void reject(HttpServletRequest request, HttpServletResponse response, ApiException exception) {
        resolver.resolveException(request, response, null, exception);
    }

    private static final class KeyAuthentication extends AbstractAuthenticationToken {

        private final PartnerPrincipal principal;

        KeyAuthentication(PartnerPrincipal principal) {
            super(List.of());
            this.principal = principal;
            setAuthenticated(true);
        }

        @Override
        public Object getCredentials() {
            return "";
        }

        @Override
        public Object getPrincipal() {
            return principal;
        }
    }
}
