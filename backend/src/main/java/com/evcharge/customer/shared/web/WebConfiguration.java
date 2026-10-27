package com.evcharge.customer.shared.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

@Configuration
class WebConfiguration implements WebMvcConfigurer {

    private final CustomerPrincipalResolver principals;

    WebConfiguration(CustomerPrincipalResolver principals) {
        this.principals = principals;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(principals);
    }

    @Bean
    LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setSupportedLocales(List.of(Locale.FRENCH, Locale.ENGLISH));
        resolver.setDefaultLocale(Locale.FRENCH);
        return resolver;
    }
}
