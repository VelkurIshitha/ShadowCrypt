package com.shadowcrypt.crypto.hashing;

import com.shadowcrypt.config.CryptoConstants;
import com.shadowcrypt.exception.CryptoOperationException;
import com.shadowcrypt.util.Bytes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/** SHA-256 hashing for files, recovered payloads, and public-key fingerprints. */
public final class Sha256 {

    private static final int BUFFER_BYTES = 64 * 1024;

    private Sha256() {
    }

    public static byte[] hash(byte[] data) {
        Objects.requireNonNull(data, "data");
        try {
            return digest().digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoOperationException("SHA-256 is not available in this Java runtime", e);
        }
    }

    public static byte[] hash(Path file) {
        Objects.requireNonNull(file, "file");
        try (InputStream input = Files.newInputStream(file)) {
            MessageDigest digest = digest();
            byte[] buffer = new byte[BUFFER_BYTES];
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return digest.digest();
        } catch (IOException e) {
            throw new CryptoOperationException("Unable to hash file: " + file, e);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoOperationException("SHA-256 is not available in this Java runtime", e);
        }
    }

    public static String hex(byte[] data) {
        return Bytes.toHex(hash(data));
    }

    public static String hex(Path file) {
        return Bytes.toHex(hash(file));
    }

    public static boolean matches(byte[] expected, byte[] actual) {
        return Bytes.constantTimeEquals(expected, actual);
    }

    private static MessageDigest digest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(CryptoConstants.SHA_256);
    }
}
