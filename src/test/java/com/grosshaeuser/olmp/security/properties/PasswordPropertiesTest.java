package com.grosshaeuser.olmp.security.properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordPropertiesTest {

    @ParameterizedTest
    @CsvSource({
            "false, false, false, abcdefgh, true",
            "false, false, false, abcdefg, false",
            "false, false, false, abcdefghi, true",
            "true, false, false, Abcdefgh, true",
            "true, false, false, abcdefgh, false",
            "true, false, false, ABCDEFGH, false",
            "false, true, false, abcdefg!, true",
            "false, true, false, abcdefgh, false",
            "false, false, true, abcdefg1, true",
            "false, false, true, abcdefgh, false",
            "true, true, false, Abcdefg!, true",
            "true, false, true, Abcdefg1, true",
            "false, true, true, abcdef1!, true",
            "true, true, true, Abcdef1!, true",
            "true, true, true, Abcd1!, false",
            "true, true, true, abcdef1!, false",
            "true, true, true, ABCDEF1!, false",
            "true, true, true, Abcdefg!, false",
            "true, true, true, Abcdefg1, false"
    })
    void patternEnforcesOnlyConfiguredRequirements(
            boolean mixedCase, boolean special, boolean number, String password, boolean expected) {
        PasswordProperties properties = new PasswordProperties(8, mixedCase, special, number);

        assertThat(password.matches(properties.getRegExPattern())).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"!", "_", "@", "-", "."})
    void punctuationSatisfiesSpecialCharacterRequirement(String punctuation) {
        var properties = new PasswordProperties(8, true, true, true);

        assertThat(("Abcdef1" + punctuation).matches(properties.getRegExPattern())).isTrue();
    }

    @Test
    void minimumLengthIsConfigurable() {
        var properties = new PasswordProperties(12, false, false, false);

        assertThat("a".repeat(11).matches(properties.getRegExPattern())).isFalse();
        assertThat("a".repeat(12).matches(properties.getRegExPattern())).isTrue();
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "false|false|false|Password must contain: at least 10 characters",
            "true|false|false|Password must contain: at least 10 characters, mixed upper- and lowercase letters",
            "false|true|false|Password must contain: at least 10 characters, a special character",
            "false|false|true|Password must contain: at least 10 characters, a number",
            "true|true|true|Password must contain: at least 10 characters, mixed upper- and lowercase letters, a number, a special character"
    })
    void descriptionListsConfiguredRequirements(boolean mixedCase, boolean special, boolean number, String expected) {
        assertThat(new PasswordProperties(10, mixedCase, special, number).getPasswordRequirementDescription())
                .isEqualTo(expected);
    }
}
