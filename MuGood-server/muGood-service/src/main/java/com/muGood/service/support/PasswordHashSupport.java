package com.muGood.service.support;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

public final class PasswordHashSupport {
    private static final String PREFIX = "sha256";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordHashSupport() {
    }

    public static String hashPassword(String password) {
        byte[] salt = new byte[16];
        SECURE_RANDOM.nextBytes(salt);
        String saltHex = HexFormat.of().formatHex(salt);
        String hashHex = sha256(saltHex + ":" + password);
        return PREFIX + "$" + saltHex + "$" + hashHex;
    }

    public static boolean matches(String password, String storedHash) {
        if (storedHash == null || !storedHash.startsWith(PREFIX + "$")) {
            return false;
        }
        String[] parts = storedHash.split("\\$");
        if (parts.length != 3) {
            return false;
        }
        String expectedHash = sha256(parts[1] + ":" + password);
        return MessageDigest.isEqual(expectedHash.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8));
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", exception);
        }
    }
}
