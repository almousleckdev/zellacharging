package com.evcharge.customer.shared.tenancy;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
class TenantDataSourcePostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if ("dataSource".equals(beanName) && bean instanceof DataSource dataSource
                && !(bean instanceof TenantDataSource)) {
            return new TenantDataSource(dataSource);
        }
        return bean;
    }
}
