package com.evcharge.customer.shared.web;

import com.evcharge.customer.shared.ActiveSessions;
import com.evcharge.customer.shared.CustomerStanding;
import com.evcharge.customer.shared.CountryDirectory;
import com.evcharge.customer.shared.RateLimiter;
import com.evcharge.customer.shared.SecurityHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableConfigurationProperties(RateLimitSettings.class)
class SecurityConfiguration {

    private static final long MAX_REQUEST_BYTES = 2_000_000;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityProblemHandler problems,
                                            CountryDirectory countries, ActiveSessions activeSessions, CustomerStanding standing,
                                            RateLimiter limiter,
                                            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver,
                                            RateLimitSettings rateLimits) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .headers(SecurityHeaders::apply)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v1/auth/**", "/v1/admin/auth/**", "/v1/payments/callback",
                                "/v1/ocpp/webhooks").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/contact", "/").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> { }).authenticationEntryPoint(problems)
                        .accessDeniedHandler(problems))
                .addFilterBefore(new RequestSizeFilter(resolver, MAX_REQUEST_BYTES), BearerTokenAuthenticationFilter.class)
                .addFilterAfter(new TenantFilter(countries, problems), BearerTokenAuthenticationFilter.class)
                .addFilterAfter(new ActiveSessionFilter(activeSessions, standing, resolver), TenantFilter.class)
                .addFilterAfter(new RateLimitFilter(limiter, resolver, rateLimits), TenantFilter.class)
                .exceptionHandling(errors -> errors.authenticationEntryPoint(problems)
                        .accessDeniedHandler(problems))
                .build();
    }
}
