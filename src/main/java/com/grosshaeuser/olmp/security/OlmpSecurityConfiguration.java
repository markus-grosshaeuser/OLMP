package com.grosshaeuser.olmp.security;

import com.grosshaeuser.olmp.security.entities.Privilege;
import com.grosshaeuser.olmp.security.repositories.UserRepository;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.stream.Stream;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
public class OlmpSecurityConfiguration {
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public OlmpUserDetailsService userDetailsService(UserRepository userRepository) {
        return new OlmpUserDetailsService() {
            @Override
            @NonNull
            public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
                return userRepository.findByUsernameWithRolesAndPrivileges(username)
                        .map(this::createFromUserInstance)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            }

            public UserDetails fromUserInstance(com.grosshaeuser.olmp.security.entities.User user) {
                return createFromUserInstance(user);
            }

            private UserDetails createFromUserInstance(com.grosshaeuser.olmp.security.entities.User user) {
                String[] authorities = user.getRoles().stream()
                        .flatMap(role -> Stream.concat(
                                Stream.of("ROLE_" + role.getName()),
                                role.getPrivileges().stream().map(Privilege::getName)
                        ))
                        .distinct()
                        .toArray(String[]::new);
                return User.withUsername(user.getUsername())
                        .password(user.getPassword())
                        .authorities(authorities)
                        .build();
            }
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }
}
