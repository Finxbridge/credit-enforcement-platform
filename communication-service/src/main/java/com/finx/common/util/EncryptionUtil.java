package com.finx.common.util;

import com.finx.common.service.ConfigCacheService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.spec.IvParameterSpec;

import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;

/**
 * Utility for encrypting/decrypting API keys and secrets
 * TODO: Use proper key management service in production
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EncryptionUtil {

    private final ConfigCacheService configCacheService;

    private SecretKey secretKey;
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_SIZE = 16; // 16 bytes for AES
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String ENCRYPTION_SECRET_KEY = "ENCRYPTION_SECRET_KEY";
    private static final String DEFAULT_ENCRYPTION_SECRET = "FinxBridge2025!!FinxBridge2025!!";

    @PostConstruct
    public void init() {
        String secret = configCacheService.getConfigOrDefault(ENCRYPTION_SECRET_KEY, DEFAULT_ENCRYPTION_SECRET);
        // Ensure key is 16 or 32 bytes for AES-128 or AES-256
        if (secret.length() < 16) {
            secret = String.format("%-16s", secret).replace(' ', '#'); // Pad to 16 bytes
        } else if (secret.length() < 32) {
            secret = String.format("%-32s", secret).replace(' ', '#'); // Pad to 32 bytes
        }
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "AES");
    }

    /**
     * Encrypt text
     */
    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = new byte[IV_SIZE];
            secureRandom.nextBytes(iv); // Generate random IV
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(ENCRYPT_MODE, secretKey, ivSpec); // Initialize with IV
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Prepend IV to encrypted data
            byte[] encryptedIvAndText = new byte[IV_SIZE + encrypted.length];
            System.arraycopy(iv, 0, encryptedIvAndText, 0, IV_SIZE);
            System.arraycopy(encrypted, 0, encryptedIvAndText, IV_SIZE, encrypted.length);

            return Base64.getEncoder().encodeToString(encryptedIvAndText);
        } catch (GeneralSecurityException e) {
            log.error("Encryption failed", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypt text
     */
    public String decrypt(String encryptedText) {
        try {
            byte[] decodedEncryptedText = Base64.getDecoder().decode(encryptedText);

            byte[] iv = new byte[IV_SIZE];
            System.arraycopy(decodedEncryptedText, 0, iv, 0, IV_SIZE);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(DECRYPT_MODE, secretKey, ivSpec); // Initialize with IV

            byte[] actualEncryptedText = new byte[decodedEncryptedText.length - IV_SIZE];
            System.arraycopy(decodedEncryptedText, IV_SIZE, actualEncryptedText, 0, actualEncryptedText.length);

            byte[] decrypted = cipher.doFinal(actualEncryptedText);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            log.error("Decryption failed", e);
            // If decryption fails, return original (might not be encrypted)
            return encryptedText;
        }
    }
}
