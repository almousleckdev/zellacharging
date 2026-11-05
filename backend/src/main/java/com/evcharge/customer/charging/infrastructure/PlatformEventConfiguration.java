package com.evcharge.customer.charging.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
class PlatformEventConfiguration {

    @Bean(name = "platformEventExecutor", destroyMethod = "")
    Executor platformEventExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
