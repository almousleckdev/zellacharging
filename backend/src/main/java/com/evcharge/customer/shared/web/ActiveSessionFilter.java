package com.evcharge.customer.shared.web;

import com.evcharge.customer.shared.ActiveSessions;
import com.evcharge.customer.shared.AdminAudience;
import com.evcharge.customer.shared.ApiException;
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
import java.util.UUID;

class ActiveSessionFilter extends OncePerRequestFilter {

    static final String CLAIM = "sid";

    private final ActiveSessions sessions;
    private final HandlerExceptionResolver resolver;

    ActiveSessionFilter(ActiveSessions sessions, HandlerExceptionResolver resolver) {
        this.sessions = sessions;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt
                && !AdminAudience.isAccessToken(jwt) && isReplaced(jwt)) {
            response.setHeader("WWW-Authenticate", "Bearer");
            resolver.resolveException(request, response, null, new ApiException(HttpStatus.UNAUTHORIZED,
                    "SESSION_REPLACED", "This account is signed in on another device"));
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isReplaced(Jwt jwt) {
        String session = jwt.getClaimAsString(CLAIM);
        return session != null && !sessions.isActive(UUID.fromString(session));
    }
}
