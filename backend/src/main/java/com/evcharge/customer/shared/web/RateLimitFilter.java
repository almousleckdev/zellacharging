package com.evcharge.customer.shared.web;

import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.RateLimiter;
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
import java.time.Duration;
import java.util.List;

class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiter limiter;
    private final HandlerExceptionResolver resolver;
    private final List<RateRule> rules;

    RateLimitFilter(RateLimiter limiter, HandlerExceptionResolver resolver, RateLimitSettings settings) {
        this.limiter = limiter;
        this.resolver = resolver;
        this.rules = rulesFor(settings);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        for (RateRule rule : rules) {
            if (rule.matches(request.getMethod(), request.getRequestURI())
                    && !limiter.tryAcquire(rule.bucket(), keyOf(rule, request), rule.limit(), rule.window())) {
                response.setHeader("Retry-After", Long.toString(rule.window().toSeconds()));
                resolver.resolveException(request, response, null, new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                        "TOO_MANY_REQUESTS", "Too many requests"));
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private static String keyOf(RateRule rule, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (rule.perCustomer() && authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return "customer:" + jwt.getSubject();
        }
        return "ip:" + request.getRemoteAddr();
    }

    private static List<RateRule> rulesFor(RateLimitSettings s) {
        Duration minute = Duration.ofMinutes(1);
        Duration hour = Duration.ofHours(1);
        return List.of(
                new RateRule("POST", "/v1/auth/login", s.loginPerIpPerMinute(), minute, false),
                new RateRule("POST", "/v1/auth/social", s.loginPerIpPerMinute(), minute, false),
                new RateRule("POST", "/v1/admin/auth/login", s.loginPerIpPerMinute(), minute, false),
                new RateRule("POST", "/v1/admin/auth/totp", s.loginPerIpPerMinute(), minute, false),
                new RateRule("POST", "/v1/admin/auth/refresh", s.refreshPerIpPerMinute(), minute, false),
                new RateRule("POST", "/v1/admin/me/password", s.loginPerIpPerMinute(), minute, false),
                new RateRule("GET", "/v1/admin/auth/invitation", s.loginPerIpPerMinute(), minute, false),
                new RateRule("POST", "/v1/admin/auth/invitation", s.loginPerIpPerMinute(), minute, false),
                new RateRule("POST", "/v1/auth/register", s.registerPerIpPerHour(), hour, false),
                new RateRule("POST", "/v1/auth/verify/resend", s.resendPerIpPerHour(), hour, false),
                new RateRule("POST", "/v1/auth/password/forgot", s.resendPerIpPerHour(), hour, false),
                new RateRule("POST", "/v1/auth/password/reset", s.verifyPerIpPerMinute(), minute, false),
                new RateRule("POST", "/v1/auth/verify", s.verifyPerIpPerMinute(), minute, false),
                new RateRule("POST", "/v1/auth/refresh", s.refreshPerIpPerMinute(), minute, false),
                new RateRule("POST", "/v1/payments/callback", s.callbackPerIpPerMinute(), minute, false),
                new RateRule("POST", "/v1/wallet/topups", s.topupPerCustomerPerMinute(), minute, true),
                new RateRule("POST", "/v1/sessions", s.sessionStartPerCustomerPerMinute(), minute, true),
                new RateRule("POST", "/v1/wallet/withdrawals", s.withdrawalPerCustomerPerMinute(), minute, true));
    }
}
