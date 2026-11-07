package com.shadowcrypt.model;

import java.nio.file.Path;
import java.security.PublicKey;
import java.util.Objects;

/** Result of a key operation, including the display fingerprint and file locations. */
public record KeyPairInfo(PublicKey publicKey, String fingerprint, Path privateKeyFile, Path publicKeyFile) {

    public KeyPairInfo {
        Objects.requireNonNull(publicKey, "publicKey");
        Objects.requireNonNull(fingerprint, "fingerprint");
    }
}
