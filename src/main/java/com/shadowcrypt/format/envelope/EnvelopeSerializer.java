package com.shadowcrypt.format.envelope;

import com.shadowcrypt.config.CryptoConstants;
import com.shadowcrypt.config.PackageConstants;
import com.shadowcrypt.format.serialization.BigEndianWriter;
import com.shadowcrypt.util.Bytes;

import java.util.Objects;

/**
 * The single source of truth for the outer envelope byte layout and for the
 * canonical AAD. AAD is exactly {@code header || wrappedKey || nonce}; ciphertext
 * is authenticated by GCM and never included.
 */
public final class EnvelopeSerializer {

    public static final int AAD_BYTES = PackageConstants.BOOTSTRAP_HEADER_BYTES
            + CryptoConstants.RSA_WRAPPED_KEY_BYTES
            + CryptoConstants.GCM_NONCE_BYTES;

    private EnvelopeSerializer() {
    }

    public static byte[] bootstrapHeader(Envelope envelope) {
        Objects.requireNonNull(envelope, "envelope");
        return header(envelope.flags(), envelope.wrappedKey().length,
                envelope.nonce().length, envelope.ciphertext().length);
    }

    /** Canonical AAD built before the ciphertext exists. */
    public static byte[] aadBytes(int flags, byte[] wrappedKey, byte[] nonce, int ciphertextLength) {
        Objects.requireNonNull(wrappedKey, "wrappedKey");
        Objects.requireNonNull(nonce, "nonce");
        return Bytes.concat(header(flags, wrappedKey.length, nonce.length, ciphertextLength),
                wrappedKey, nonce);
    }

    public static byte[] aadBytes(Envelope envelope) {
        Objects.requireNonNull(envelope, "envelope");
        return Bytes.concat(bootstrapHeader(envelope), envelope.wrappedKey(), envelope.nonce());
    }

    public static byte[] serialize(Envelope envelope) {
        Objects.requireNonNull(envelope, "envelope");
        return Bytes.concat(bootstrapHeader(envelope), envelope.wrappedKey(),
                envelope.nonce(), envelope.ciphertext());
    }

    private static byte[] header(int flags, int wrappedKeyLength, int nonceLength, int ciphertextLength) {
        int totalLength = PackageConstants.BOOTSTRAP_HEADER_BYTES
                + wrappedKeyLength + nonceLength + ciphertextLength;
        return new BigEndianWriter()
                .writeBytes(PackageConstants.MAGIC)
                .writeByte(PackageConstants.VERSION)
                .writeByte(flags)
                .writeInt(totalLength)
                .writeShort(wrappedKeyLength)
                .writeByte(nonceLength)
                .writeInt(ciphertextLength)
                .toByteArray();
    }
}
