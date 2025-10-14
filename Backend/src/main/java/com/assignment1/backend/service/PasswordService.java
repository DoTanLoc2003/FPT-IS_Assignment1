package com.assignment1.backend.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordService.class);
    private static final int SALT_LENGTH = 16; // 16 bytes
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String ENCODING = "UTF-8";

    public String hashPassword(String rawPassword) {
        if(rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(rawPassword.getBytes(ENCODING));

            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Hash algorithm not available: {}", HASH_ALGORITHM, e);
            throw new RuntimeException("Hash algorithm not available", e);
        } catch (Exception e) {
            logger.error("Error hashing password", e);
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    public boolean checkPassword(String rawPassword, String storedHash) {
        if(rawPassword == null || rawPassword.isEmpty() || storedHash == null || storedHash.isEmpty()) {
            logger.warn("Null password or hash provided for verification");
            return false;
        }

        if(rawPassword.trim().isEmpty() || storedHash.trim().isEmpty()) {
            logger.warn("Empty password or hash provided for verification");
            return false;
        }

        try {
            byte[] combined = Base64.getDecoder().decode(storedHash);
            if (combined.length < SALT_LENGTH) {
                logger.warn("Invalid stored hash length");
                return false;
            }

            byte[] salt = new byte[SALT_LENGTH];
            byte[] storedHashedPassword = new byte[combined.length - SALT_LENGTH];
            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);
            System.arraycopy(combined, SALT_LENGTH, storedHashedPassword, 0, storedHashedPassword.length);

            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            byte[] inputPasswordHash = md.digest(rawPassword.getBytes(ENCODING));

            return MessageDigest.isEqual(inputPasswordHash, storedHashedPassword);
        } catch (IllegalArgumentException e) {
            logger.error("Stored hash is not valid Base64", e);
            return false;
        } catch (NoSuchAlgorithmException e) {
            logger.error("Hash algorithm not available: {}", HASH_ALGORITHM, e);
            return false;
        } catch (Exception e) {
            logger.error("Error verifying password", e);
            return false;
        }
    }

}
