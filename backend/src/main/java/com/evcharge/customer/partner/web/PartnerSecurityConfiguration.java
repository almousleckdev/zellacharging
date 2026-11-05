package com.evcharge.customer.partner.web;

import com.evcharge.customer.partner.application.PartnerKeyService;
import com.evcharge.customer.partner.application.PartnerSettings;
import com.evcharge.customer.shared.CountryDirectory;
import com.evcharge.customer.shared.RateLimiter;
import com.evcharge.customer.shared.SecurityHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
class PartnerSecurityConfiguration {

    @Bean
    @Order(1)
    SecurityFilterChain partnerSecurityFilterChain(HttpSecurity http, PartnerKeyService keys,
                                                   CountryDirectory countries, RateLimiter limiter,
                                                   PartnerSettings settings,
                                                   @Qualifier("handlerExceptionResolver")
                                                   HandlerExceptionResolver resolver) throws Exception {
        return http
                .securityMatcher("/v1/partners/**")
                .csrf(csrf -> csrf.disable())
                .headers(SecurityHeaders::apply)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .addFilterBefore(new PartnerKeyFilter(keys, countries, limiter, settings, resolver),
                        AuthorizationFilter.class)
                .build();
    }
}
