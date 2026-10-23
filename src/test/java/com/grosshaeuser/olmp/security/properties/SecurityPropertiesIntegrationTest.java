package com.grosshaeuser.olmp.security.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityPropertiesIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfiguration.class);

    @Test
    void bindsJwtPropertiesIncludingDuration() {
        contextRunner.withPropertyValues(
                "olmp.security.jwt.issuer=test-issuer",
                "olmp.security.jwt.secret=test-secret",
                "olmp.security.jwt.access-token-ttl=PT17M")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(JwtProperties.class))
                            .isEqualTo(new JwtProperties("test-issuer", "test-secret", Duration.ofMinutes(17)));
                });
    }

    @Test
    void bindsPasswordPropertiesUsingKebabCaseNames() {
        contextRunner.withPropertyValues(
                "olmp.security.password.min-length=14",
                "olmp.security.password.mixed-case-required=true",
                "olmp.security.password.special-character-required=false",
                "olmp.security.password.number-required=true")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(PasswordProperties.class))
                            .isEqualTo(new PasswordProperties(14, true, false, true));
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties({JwtProperties.class, PasswordProperties.class})
    static class PropertiesConfiguration {
    }
}
