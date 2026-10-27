package com.evcharge.customer.shared.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Component
class ErrorMessages {

    private final MessageSource messages;
    private final LocaleResolver localeResolver;

    ErrorMessages(MessageSource messages, LocaleResolver localeResolver) {
        this.messages = messages;
        this.localeResolver = localeResolver;
    }

    String forCode(String code, String fallback) {
        return messages.getMessage(code, null, fallback, currentLocale());
    }

    private Locale currentLocale() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            return localeResolver.resolveLocale(request);
        }
        return LocaleContextHolder.getLocale();
    }
}
