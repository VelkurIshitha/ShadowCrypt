package com.shadowcrypt.crypto.aes;

import com.shadowcrypt.config.CryptoConstants;
import com.shadowcrypt.exception.CryptoOperationException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/** One-time AES-256 session key with its own fresh GCM nonce. */
public final class AesSessionKey {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final SecretKey key;
    private final byte[] nonce;

    private AesSessionKey(SecretKey key, byte[] nonce) {
        this.key = key;
        this.nonce = nonce;
    }

    public static AesSessionKey generate() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance(CryptoConstants.AES_ALGORITHM);
            generator.init(CryptoConstants.AES_KEY_BITS, RANDOM);
            SecretKey key = generator.generateKey();

            byte[] nonce = new byte[CryptoConstants.GCM_NONCE_BYTES];
            RANDOM.nextBytes(nonce);

            return new AesSessionKey(key, nonce);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoOperationException("AES is not available in this Java runtime", e);
        }
    }

    public SecretKey key() {
        return key;
    }

    public byte[] nonce() {
        return nonce.clone();
    }
}
