package com.bankingpoc.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class Hashing {
    private Hashing() {
    }

    public static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexValue = new StringBuilder();
            for (byte hashedByte : hashedBytes) {
                hexValue.append(String.format("%02x", hashedByte));
            }
            return hexValue.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 hashing is unavailable", exception);
        }
    }
}
