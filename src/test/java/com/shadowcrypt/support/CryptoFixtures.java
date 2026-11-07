package com.shadowcrypt.support;

import com.shadowcrypt.crypto.rsa.RsaKeyPairGenerator;

import java.security.KeyPair;

/**
 * Shared RSA-3072 fixtures. Generating a key pair is the slowest part of the test
 * suite, so the pairs are created once and reused.
 */
public final class CryptoFixtures {

    private static volatile KeyPair keyPair;
    private static volatile KeyPair otherKeyPair;

    private CryptoFixtures() {
    }

    public static KeyPair rsaKeyPair() {
        KeyPair local = keyPair;
        if (local == null) {
            synchronized (CryptoFixtures.class) {
                local = keyPair;
                if (local == null) {
                    local = RsaKeyPairGenerator.generate();
                    keyPair = local;
                }
            }
        }
        return local;
    }

    /** A second, unrelated key pair used for wrong-key scenarios. */
    public static KeyPair otherRsaKeyPair() {
        KeyPair local = otherKeyPair;
        if (local == null) {
            synchronized (CryptoFixtures.class) {
                local = otherKeyPair;
                if (local == null) {
                    local = RsaKeyPairGenerator.generate();
                    otherKeyPair = local;
                }
            }
        }
        return local;
    }
}
