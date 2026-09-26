package com.grosshaeuser.olmp.security;

import com.grosshaeuser.olmp.security.entities.Privilege;
import com.grosshaeuser.olmp.security.entities.Role;
import com.grosshaeuser.olmp.security.repositories.UserRepository;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
public class OlmpSecurityConfiguration {
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return new UserDetailsService() {
            @Override
            @NonNull
            public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
                return userRepository.findByUsernameWithRolesAndPrivileges(username)
                        .map(user ->
                                User.withUsername(user.getUsername())
                                        .password(user.getPassword())
                                        .roles(user.getRoles().stream()
                                                .map(Role::getName)
                                                .toArray(String[]::new))
                                        .authorities(
                                                user.getRoles().stream()
                                                        .flatMap(role -> role.getPrivileges().stream())
                                                        .map(Privilege::getName)
                                                        .distinct()
                                                        .toArray(String[]::new))
                                        .build()
                        )
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            }
        };
    }
}
