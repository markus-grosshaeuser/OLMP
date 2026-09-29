package com.grosshaeuser.olmp.security.services;

import com.grosshaeuser.olmp.security.entities.ApiKey;
import com.grosshaeuser.olmp.security.entities.User;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class ApiKeyGeneratorImpl implements ApiKeyGenerator {
    private static final int SECRET_LENGTH_IN_BYTES = 32;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public ApiKey generateApiKeyForUser(User user) {
        UUID uuid = UUID.randomUUID();
        String secret = generateSecret();
        OffsetDateTime createdAt = OffsetDateTime.now();
        OffsetDateTime expiresAt = createdAt.plusMonths(6);
        return new ApiKey(uuid, secret, createdAt, expiresAt, user);
    }

    private String generateSecret() {
        byte[] secretBytes = new byte[SECRET_LENGTH_IN_BYTES];
        secureRandom.nextBytes(secretBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(secretBytes);
    }
}
