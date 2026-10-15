package com.grosshaeuser.olmp.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "olmp.security.jwt")
public record JwtProperties(
        String issuer,
        String secret,
        Duration accessTokenTtl
) {
}
