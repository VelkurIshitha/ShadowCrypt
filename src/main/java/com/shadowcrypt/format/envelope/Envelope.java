package com.shadowcrypt.format.envelope;

import com.shadowcrypt.config.CryptoConstants;
import com.shadowcrypt.config.PackageConstants;

import java.util.Objects;

/**
 * Readable outer envelope: the RSA-wrapped AES key, the GCM nonce, and the
 * authenticated ciphertext. Metadata stays inside the ciphertext.
 */
public record Envelope(int flags, byte[] wrappedKey, byte[] nonce, byte[] ciphertext) {

    public Envelope {
        if (flags < 0 || flags > 0xFF) {
            throw new IllegalArgumentException("Flags must be a single unsigned byte");
        }
        Objects.requireNonNull(wrappedKey, "wrappedKey");
        Objects.requireNonNull(nonce, "nonce");
        Objects.requireNonNull(ciphertext, "ciphertext");
        if (wrappedKey.length != CryptoConstants.RSA_WRAPPED_KEY_BYTES) {
            throw new IllegalArgumentException("Wrapped key must be "
                    + CryptoConstants.RSA_WRAPPED_KEY_BYTES + " bytes");
        }
        if (nonce.length != CryptoConstants.GCM_NONCE_BYTES) {
            throw new IllegalArgumentException("Nonce must be "
                    + CryptoConstants.GCM_NONCE_BYTES + " bytes");
        }
        if (ciphertext.length < CryptoConstants.GCM_TAG_BYTES) {
            throw new IllegalArgumentException("Ciphertext must include the "
                    + CryptoConstants.GCM_TAG_BYTES + "-byte authentication tag");
        }
        wrappedKey = wrappedKey.clone();
        nonce = nonce.clone();
        ciphertext = ciphertext.clone();
    }

    @Override
    public byte[] wrappedKey() {
        return wrappedKey.clone();
    }

    @Override
    public byte[] nonce() {
        return nonce.clone();
    }

    @Override
    public byte[] ciphertext() {
        return ciphertext.clone();
    }

    public int totalLength() {
        return PackageConstants.BOOTSTRAP_HEADER_BYTES + wrappedKey.length + nonce.length + ciphertext.length;
    }
}
