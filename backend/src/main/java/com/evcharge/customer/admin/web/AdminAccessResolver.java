package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminAccessService;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.shared.AdminAudience;
import com.evcharge.customer.shared.ApiException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.UUID;

@Component
class AdminAccessResolver implements HandlerMethodArgumentResolver {

    private final AdminAccessService access;

    AdminAccessResolver(AdminAccessService access) {
        this.access = access;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AdminAccess.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container, NativeWebRequest request, WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)
                || !AdminAudience.isAccessToken(jwt)) {
            throw unauthenticated();
        }
        try {
            return access.load(UUID.fromString(jwt.getSubject()));
        } catch (IllegalArgumentException e) {
            throw unauthenticated();
        }
    }

    private static ApiException unauthenticated() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentication required");
    }
}
