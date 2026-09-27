package com.grosshaeuser.olmp.security.filterchains;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
public class InternalUiFilterChain {
    @Bean
    @Order(3)
    SecurityFilterChain vaadinFilterChain(HttpSecurity http) {
        http.with(VaadinSecurityConfigurer.vaadin(), configurer -> configurer
                .loginView("/login")
        );
        return http.build();
    }
}
