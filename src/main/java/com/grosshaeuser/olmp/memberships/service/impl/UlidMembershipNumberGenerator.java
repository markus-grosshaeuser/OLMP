package com.grosshaeuser.olmp.memberships.service.impl;

import com.grosshaeuser.olmp.memberships.service.MembershipNumberGenerator;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

@Component
public class UlidMembershipNumberGenerator implements MembershipNumberGenerator {
    private static final char[] ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String generate() {
        long timestamp = System.currentTimeMillis();
        byte[] randomBytes = new byte[10];
        RANDOM.nextBytes(randomBytes);

        ByteBuffer buffer = ByteBuffer.allocate(16);

        buffer.put((byte) (timestamp >> 40));
        buffer.put((byte) (timestamp >> 32));
        buffer.put((byte) (timestamp >> 24));
        buffer.put((byte) (timestamp >> 16));
        buffer.put((byte) (timestamp >> 8));
        buffer.put((byte) timestamp);

        buffer.put(randomBytes);
        byte[] bytes = buffer.array();

        return encodeBase32(bytes);
    }

    private String encodeBase32(byte[] bytes) {
        StringBuilder sb = new StringBuilder(26);

        int bitBuffer = 0;
        int bitCount = 0;

        for (byte b : bytes) {
            bitBuffer = (bitBuffer << 8) | (b & 0xFF);
            bitCount += 8;

            while (bitCount >= 5) {
                int index = (bitBuffer >> (bitCount - 5)) & 0x1F;
                sb.append(ALPHABET[index]);
                bitCount -= 5;
            }
        }

        if (bitCount > 0) {
            int index = (bitBuffer << (5 - bitCount)) & 0x1F;
            sb.append(ALPHABET[index]);
        }

        return sb.substring(0, 26);
    }
}
