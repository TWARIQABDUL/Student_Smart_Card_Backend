package com.student_smart_pay.student_management.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CryptoService {

    @Value("${app.aes.secret}")
    private String secretKey; // Must be 32 characters for AES-256

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12; // Standard for GCM
    private static final int TAG_LENGTH = 128;

    // 🔒 ROBUST ENCRYPT
    public String encrypt(String value) {
        try {
            // 1. Generate a random IV (Initialization Vector)
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            // 2. Prepare Key & Cipher
            SecretKey key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            // 3. Encrypt
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

            // 4. Combine IV + CipherText (We need the IV to decrypt later!)
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getUrlEncoder().withoutPadding().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Encryption Failed", e);
        }
    }

    // 🔓 ROBUST DECRYPT
    public String decrypt(String encryptedValue) {
        try {
            // 1. Decode Base64
            byte[] decoded = Base64.getUrlDecoder().decode(encryptedValue);

            // 2. Extract IV (First 12 bytes)
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, iv.length);

            // 3. Extract CipherText (The rest)
            byte[] ciphertext = new byte[decoded.length - IV_LENGTH];
            System.arraycopy(decoded, IV_LENGTH, ciphertext, 0, ciphertext.length);

            // 4. Decrypt
            SecretKey key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption Failed (Invalid Token)", e);
        }
    }
}