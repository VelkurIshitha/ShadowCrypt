package com.shadowcrypt.crypto.rsa;

import com.shadowcrypt.config.CryptoConstants;
import com.shadowcrypt.exception.CryptoOperationException;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/** RSA-3072 key-pair generation with a platform SecureRandom. */
public final class RsaKeyPairGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private RsaKeyPairGenerator() {
    }

    public static KeyPair generate() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(CryptoConstants.RSA_ALGORITHM);
            generator.initialize(CryptoConstants.RSA_KEY_BITS, RANDOM);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoOperationException("RSA is not available in this Java runtime", e);
        }
    }
}
