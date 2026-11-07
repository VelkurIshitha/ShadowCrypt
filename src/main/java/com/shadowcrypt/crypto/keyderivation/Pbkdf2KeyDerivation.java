package com.shadowcrypt.crypto.keyderivation;

import com.shadowcrypt.config.CryptoConstants;
import com.shadowcrypt.exception.CryptoOperationException;
import com.shadowcrypt.util.SecureWipe;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Objects;

/** PBKDF2-HMAC-SHA256 derivation of the AES key that protects a private-key file. */
public final class Pbkdf2KeyDerivation {

    private Pbkdf2KeyDerivation() {
    }

    public static SecretKey deriveKey(char[] passphrase, byte[] salt) {
        return deriveKey(passphrase, salt, CryptoConstants.PBKDF2_ITERATIONS);
    }

    public static SecretKey deriveKey(char[] passphrase, byte[] salt, int iterations) {
        Objects.requireNonNull(passphrase, "passphrase");
        Objects.requireNonNull(salt, "salt");
        if (passphrase.length == 0) {
            throw new IllegalArgumentException("The passphrase must not be empty");
        }
        if (salt.length < 8) {
            throw new IllegalArgumentException("The salt must be at least 8 bytes");
        }
        if (iterations < 1) {
            throw new IllegalArgumentException("The iteration count must be positive");
        }

        PBEKeySpec specification = new PBEKeySpec(
                passphrase, salt, iterations, CryptoConstants.AES_KEY_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(CryptoConstants.PBKDF2_ALGORITHM);
            byte[] derived = factory.generateSecret(specification).getEncoded();
            try {
                return new SecretKeySpec(derived, CryptoConstants.AES_ALGORITHM);
            } finally {
                SecureWipe.wipe(derived);
            }
        } catch (GeneralSecurityException e) {
            throw new CryptoOperationException("PBKDF2 key derivation failed", e);
        } finally {
            specification.clearPassword();
        }
    }
}
