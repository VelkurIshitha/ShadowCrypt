package com.shadowcrypt.crypto.aes;

import com.shadowcrypt.config.CryptoConstants;
import com.shadowcrypt.exception.AuthenticationFailedException;
import com.shadowcrypt.exception.CryptoOperationException;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.GeneralSecurityException;
import java.util.Objects;

/**
 * AES-256-GCM with an explicit 128-bit tag. The caller supplies a fresh nonce and
 * the canonical additional authenticated data; the returned ciphertext always
 * includes the GCM tag.
 */
public final class AesGcmCipher {

    private static final int MIN_CIPHERTEXT_BYTES = CryptoConstants.GCM_TAG_BYTES;

    private AesGcmCipher() {
    }

    public static byte[] encrypt(SecretKey key, byte[] nonce, byte[] plaintext, byte[] additionalData) {
        validate(key, nonce, plaintext, additionalData);
        try {
            Cipher cipher = Cipher.getInstance(CryptoConstants.AES_TRANSFORMATION);
            GCMParameterSpec parameters = new GCMParameterSpec(CryptoConstants.GCM_TAG_BITS, nonce);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameters);
            cipher.updateAAD(additionalData);
            return cipher.doFinal(plaintext);
        } catch (GeneralSecurityException e) {
            throw new CryptoOperationException("AES-GCM encryption failed", e);
        }
    }

    public static byte[] decrypt(SecretKey key, byte[] nonce, byte[] ciphertext, byte[] additionalData) {
        validate(key, nonce, ciphertext, additionalData);
        if (ciphertext.length < MIN_CIPHERTEXT_BYTES) {
            throw new AuthenticationFailedException("The ciphertext is too short to contain an authentication tag");
        }
        try {
            Cipher cipher = Cipher.getInstance(CryptoConstants.AES_TRANSFORMATION);
            GCMParameterSpec parameters = new GCMParameterSpec(CryptoConstants.GCM_TAG_BITS, nonce);
            cipher.init(Cipher.DECRYPT_MODE, key, parameters);
            cipher.updateAAD(additionalData);
            return cipher.doFinal(ciphertext);
        } catch (AEADBadTagException e) {
            throw new AuthenticationFailedException(
                    "Authentication failed: the key, passphrase, or data is not valid", e);
        } catch (GeneralSecurityException e) {
            throw new CryptoOperationException("AES-GCM decryption failed", e);
        }
    }

    private static void validate(SecretKey key, byte[] nonce, byte[] input, byte[] additionalData) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(nonce, "nonce");
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(additionalData, "additionalData");
        if (!CryptoConstants.AES_ALGORITHM.equalsIgnoreCase(key.getAlgorithm())) {
            throw new IllegalArgumentException("The supplied key is not an AES key");
        }
        if (nonce.length != CryptoConstants.GCM_NONCE_BYTES) {
            throw new IllegalArgumentException(
                    "AES-GCM nonce must be " + CryptoConstants.GCM_NONCE_BYTES + " bytes, got " + nonce.length);
        }
    }
}
