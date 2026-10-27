package com.evcharge.customer.shared.web;

import com.evcharge.customer.shared.AdminAudience;
import com.evcharge.customer.shared.CountryDirectory;
import com.evcharge.customer.shared.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

class TenantFilter extends OncePerRequestFilter {

    static final String CLAIM = "cty";

    private final CountryDirectory countries;
    private final SecurityProblemHandler problems;

    TenantFilter(CountryDirectory countries, SecurityProblemHandler problems) {
        this.countries = countries;
        this.problems = problems;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication != null && authentication.getPrincipal() instanceof Jwt jwt)) {
            chain.doFilter(request, response);
            return;
        }
        boolean adminToken = AdminAudience.isAccessToken(jwt);
        boolean adminPath = request.getRequestURI().startsWith(AdminAudience.PATH_PREFIX);
        if (adminToken || adminPath) {
            if (adminToken && adminPath) {
                chain.doFilter(request, response);
            } else {
                problems.commence(request, response, new BadCredentialsException("Token does not match the endpoint"));
            }
            return;
        }
        String country = jwt.getClaimAsString(CLAIM);
        if (country == null || countries.activeByCode(country).isEmpty()) {
            problems.commence(request, response, new BadCredentialsException("Token has no valid tenant"));
            return;
        }
        try (TenantContext.Scope scope = TenantContext.open(country)) {
            chain.doFilter(request, response);
        }
    }
}
