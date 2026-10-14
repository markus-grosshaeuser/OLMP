package com.grosshaeuser.olmp.membership.service;

import com.grosshaeuser.olmp.memberships.service.impl.UlidMembershipNumberGenerator;
import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MembershipNumberGeneratorTest {
    private final UlidMembershipNumberGenerator generator = new UlidMembershipNumberGenerator();

    @RepeatedTest(100)
    void generate_ShouldReturnStringOfLength26() {
        String result = generator.generate();
        assertEquals(26, result.length(), "The generated ULID should have a length of 26");
    }

    @RepeatedTest(100)
    void generate_ShouldContainOnlyAllowedCharacters() {
        String result = generator.generate();
        String alphabet = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";

        for (char c : result.toCharArray()) {
            assertTrue(alphabet.indexOf(c) >= 0, "ULID contains an illegal character: " + c);
        }
    }
}
