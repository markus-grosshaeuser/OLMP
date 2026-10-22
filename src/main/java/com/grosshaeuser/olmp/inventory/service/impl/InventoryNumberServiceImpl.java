package com.grosshaeuser.olmp.inventory.service.impl;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;

import com.grosshaeuser.olmp.inventory.service.InventoryNumberService;
import org.springframework.stereotype.Service;

/*
 * Adapted from https://github.com/paralleldrive/cuid2
 * MIT License Copyright (c) 2022 Eric Elliott
 */

@Service
public class InventoryNumberServiceImpl implements InventoryNumberService {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    private static final String BASE36_CHARS = "0123456789abcdefghijklmnopqrstuvwxyz";
    private static final int DEFAULT_LENGTH = 20;
    private static final int BIG_LENGTH = 32;
    private static final long INITIAL_COUNT_MAX = 476782367L;

    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate() {
        return nextId().substring(0,3) + "-" + nextId().substring(3,8) + "-" + nextId().substring(8,12) + "-" + nextId().substring(12,17) + "-" + nextId().substring(17,20);
    }

    private String nextId() {
        AtomicLong counter = new AtomicLong(random.nextLong(INITIAL_COUNT_MAX));
        String fingerprint = generateFingerprint();
        char firstLetter = ALPHABET.charAt(random.nextInt(ALPHABET.length()));
        String time = Long.toString(System.currentTimeMillis(), 36);
        String count = Long.toString(counter.getAndIncrement(), 36);
        String salt = createEntropy(DEFAULT_LENGTH);
        String hashInput = time + salt + count + fingerprint;
        String hashed = hash(hashInput);
        String suffix = hashed.substring(1, Math.min(hashed.length(), DEFAULT_LENGTH));
        return firstLetter + suffix;
    }

    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA3-512");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            BigInteger bigInt = new BigInteger(1, digest);
            String base36 = bigInt.toString(36);
            return base36.substring(1);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA3-512 not supported on this JVM", e);
        }
    }

    private String createEntropy(int len) {
        StringBuilder sb = new StringBuilder();
        while (sb.length() < len) {
            int index = random.nextInt(BASE36_CHARS.length());
            sb.append(BASE36_CHARS.charAt(index));
        }
        return sb.toString();
    }

    private String generateFingerprint() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.getProperty("user.name"));
        sb.append(System.getProperty("os.name"));
        try {
            sb.append(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            sb.append("unknown-host");
        }
        sb.append(createEntropy(BIG_LENGTH));

        return hash(sb.toString()).substring(0, BIG_LENGTH);
    }
}
