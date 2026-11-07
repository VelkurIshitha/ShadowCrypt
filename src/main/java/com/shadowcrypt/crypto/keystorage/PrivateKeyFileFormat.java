package com.shadowcrypt.crypto.keystorage;

import com.shadowcrypt.config.CryptoConstants;
import com.shadowcrypt.exception.KeyStorageException;
import com.shadowcrypt.util.Bytes;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

/**
 * Versioned container for a passphrase-protected private key.
 *
 * <pre>
 * magic "SCPR" | version | kdfId | iterations | saltLen | salt | nonceLen | nonce | ciphertextLen | ciphertext
 * </pre>
 *
 * All integers are big-endian. The bytes preceding the ciphertext are used as GCM
 * additional authenticated data so the KDF parameters cannot be altered silently.
 */
public final class PrivateKeyFileFormat {

    public static final byte[] MAGIC = {0x53, 0x43, 0x50, 0x52}; // "SCPR"
    public static final byte VERSION = CryptoConstants.PRIVATE_KEY_FILE_VERSION;
    public static final byte KDF_ID_PBKDF2_HMAC_SHA256 = CryptoConstants.PRIVATE_KEY_KDF_PBKDF2_SHA256;

    public static final int MAX_CONTAINER_BYTES = 64 * 1024;

    private static final int MIN_ITERATIONS = 1;
    private static final int MAX_ITERATIONS = 10_000_000;

    private PrivateKeyFileFormat() {
    }

    /** Parsed contents of a protected private-key file. */
    public record Container(int iterations, byte[] salt, byte[] nonce, byte[] ciphertext) {

        public Container {
            if (iterations < MIN_ITERATIONS || iterations > MAX_ITERATIONS) {
                throw new KeyStorageException("Invalid PBKDF2 iteration count: " + iterations);
            }
            Objects.requireNonNull(salt, "salt");
            Objects.requireNonNull(nonce, "nonce");
            Objects.requireNonNull(ciphertext, "ciphertext");
            salt = salt.clone();
            nonce = nonce.clone();
            ciphertext = ciphertext.clone();
        }

        @Override
        public byte[] salt() {
            return salt.clone();
        }

        @Override
        public byte[] nonce() {
            return nonce.clone();
        }

        @Override
        public byte[] ciphertext() {
            return ciphertext.clone();
        }
    }

    /** The canonical header, also used byte-for-byte as the GCM AAD. */
    public static byte[] headerBytes(int iterations, byte[] salt, byte[] nonce, int ciphertextLength) {
        Objects.requireNonNull(salt, "salt");
        Objects.requireNonNull(nonce, "nonce");
        if (salt.length > 0xFF || nonce.length > 0xFF) {
            throw new KeyStorageException("Salt or nonce is too long for the key-file format");
        }
        ByteBuffer buffer = ByteBuffer.allocate(headerLength(salt.length, nonce.length));
        buffer.put(MAGIC);
        buffer.put(VERSION);
        buffer.put(KDF_ID_PBKDF2_HMAC_SHA256);
        buffer.putInt(iterations);
        buffer.put((byte) salt.length);
        buffer.put(salt);
        buffer.put((byte) nonce.length);
        buffer.put(nonce);
        buffer.putInt(ciphertextLength);
        return buffer.array();
    }

    public static byte[] serialize(Container container) {
        byte[] salt = container.salt();
        byte[] nonce = container.nonce();
        byte[] ciphertext = container.ciphertext();
        byte[] header = headerBytes(container.iterations(), salt, nonce, ciphertext.length);
        return Bytes.concat(header, ciphertext);
    }

    public static Container parse(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes");
        int minimumLength = headerLength(CryptoConstants.PBKDF2_SALT_BYTES, CryptoConstants.GCM_NONCE_BYTES)
                + CryptoConstants.GCM_TAG_BYTES;
        if (bytes.length < minimumLength) {
            throw new KeyStorageException("The private-key file is truncated");
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        byte[] magic = new byte[MAGIC.length];
        buffer.get(magic);
        if (!Arrays.equals(magic, MAGIC)) {
            throw new KeyStorageException("Not a ShadowCrypt private-key file (bad magic number)");
        }
        byte version = buffer.get();
        if (version != VERSION) {
            throw new KeyStorageException("Unsupported private-key file version: " + (version & 0xFF));
        }
        byte kdfId = buffer.get();
        if (kdfId != KDF_ID_PBKDF2_HMAC_SHA256) {
            throw new KeyStorageException("Unsupported key-derivation function id: " + (kdfId & 0xFF));
        }
        int iterations = buffer.getInt();
        if (iterations < MIN_ITERATIONS || iterations > MAX_ITERATIONS) {
            throw new KeyStorageException("Invalid PBKDF2 iteration count: " + iterations);
        }

        int saltLength = buffer.get() & 0xFF;
        if (saltLength != CryptoConstants.PBKDF2_SALT_BYTES || saltLength > buffer.remaining()) {
            throw new KeyStorageException("Invalid salt length in the private-key file");
        }
        byte[] salt = new byte[saltLength];
        buffer.get(salt);

        int nonceLength = buffer.get() & 0xFF;
        if (nonceLength != CryptoConstants.GCM_NONCE_BYTES || nonceLength > buffer.remaining()) {
            throw new KeyStorageException("Invalid nonce length in the private-key file");
        }
        byte[] nonce = new byte[nonceLength];
        buffer.get(nonce);

        int ciphertextLength = buffer.getInt();
        if (ciphertextLength < CryptoConstants.GCM_TAG_BYTES || ciphertextLength > MAX_CONTAINER_BYTES) {
            throw new KeyStorageException("Invalid ciphertext length in the private-key file");
        }
        if (ciphertextLength != buffer.remaining()) {
            throw new KeyStorageException("The private-key file length does not match its header");
        }
        byte[] ciphertext = new byte[ciphertextLength];
        buffer.get(ciphertext);

        return new Container(iterations, salt, nonce, ciphertext);
    }

    private static int headerLength(int saltLength, int nonceLength) {
        return MAGIC.length + 1 + 1 + 4 + 1 + saltLength + 1 + nonceLength + 4;
    }
}
