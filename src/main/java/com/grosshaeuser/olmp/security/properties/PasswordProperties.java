package com.grosshaeuser.olmp.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.StringJoiner;

@ConfigurationProperties(prefix = "olmp.security.password")
public record PasswordProperties(
        int minLength,
        boolean mixedCaseRequired,
        boolean specialCharacterRequired,
        boolean numberRequired
) {
    public String getRegExPattern() {
        StringBuilder pattern = new StringBuilder("^");
        if (mixedCaseRequired) {
            pattern.append("(?=.*[a-z])(?=.*[A-Z])");
        }
        if (numberRequired) {
            pattern.append("(?=.*\\d)");
        }
        if (specialCharacterRequired) {
            pattern.append("(?=.*[\\p{Punct}])");
        }
        pattern.append(".{").append(minLength).append(",}$");
        return pattern.toString();
    }

    public String getPasswordRequirementDescription() {
        StringJoiner joiner = new StringJoiner(", ");
        joiner.add("at least " + minLength + " characters");

        if (mixedCaseRequired) {
            joiner.add("mixed upper- and lowercase letters");
        }
        if (numberRequired) {
            joiner.add("a number");
        }
        if (specialCharacterRequired) {
            joiner.add("a special character");
        }

        return "Password must contain: " + joiner;
    }
}
