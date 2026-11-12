package com.evcharge.customer.shared.web;

import com.evcharge.customer.shared.ActiveSessions;
import com.evcharge.customer.shared.AdminAudience;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.CustomerStanding;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

class ActiveSessionFilter extends OncePerRequestFilter {

    static final String CLAIM = "sid";

    private final ActiveSessions sessions;
    private final CustomerStanding standing;
    private final HandlerExceptionResolver resolver;

    ActiveSessionFilter(ActiveSessions sessions, CustomerStanding standing, HandlerExceptionResolver resolver) {
        this.sessions = sessions;
        this.standing = standing;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt
                && !AdminAudience.isAccessToken(jwt) && isReplaced(jwt)) {
            resolver.resolveException(request, response, null, suspension(jwt).orElseGet(() -> replaced(response)));
            return;
        }
        chain.doFilter(request, response);
    }

    private static ApiException replaced(HttpServletResponse response) {
        response.setHeader("WWW-Authenticate", "Bearer");
        return new ApiException(HttpStatus.UNAUTHORIZED, "SESSION_REPLACED", "This account is signed in on another device");
    }

    private Optional<ApiException> suspension(Jwt jwt) {
        try {
            standing.requireActive(UUID.fromString(jwt.getSubject()));
            return Optional.empty();
        } catch (ApiException e) {
            return Optional.of(e);
        }
    }

    private boolean isReplaced(Jwt jwt) {
        String session = jwt.getClaimAsString(CLAIM);
        return session != null && !sessions.isActive(UUID.fromString(session));
    }
}
