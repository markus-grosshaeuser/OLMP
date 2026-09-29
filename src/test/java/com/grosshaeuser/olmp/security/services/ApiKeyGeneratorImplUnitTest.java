
package com.grosshaeuser.olmp.security.services;

import com.grosshaeuser.olmp.security.entities.ApiKey;
import com.grosshaeuser.olmp.security.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ApiKeyGeneratorImplUnitTest {

    private ApiKeyGeneratorImpl apiKeyGenerator;

    @Mock
    private User user;

    @BeforeEach
    void setUp() {
        apiKeyGenerator = new ApiKeyGeneratorImpl();
    }

    @Test
    void generateApiKeyForUser_ShouldReturnValidApiKey() {
        ApiKey apiKey = apiKeyGenerator.generateApiKeyForUser(user);

        assertThat(apiKey).isNotNull();
        assertThat(apiKey.getId()).isNotNull();
        assertThat(apiKey.getSecret()).isNotNull();

        byte[] decodedSecret = Base64.getUrlDecoder().decode(apiKey.getSecret());
        assertThat(decodedSecret).hasSize(32);

        assertThat(apiKey.getCreatedAt()).isBeforeOrEqualTo(OffsetDateTime.now());
        assertThat(apiKey.getExpiresAt()).isEqualTo(apiKey.getCreatedAt().plusMonths(6));

        assertThat(apiKey.getUser()).isEqualTo(user);
    }
}