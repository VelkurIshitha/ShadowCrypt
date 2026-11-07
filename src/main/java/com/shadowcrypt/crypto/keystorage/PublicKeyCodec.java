package com.shadowcrypt.crypto.keystorage;

import com.shadowcrypt.config.CryptoConstants;
import com.shadowcrypt.crypto.hashing.Sha256;
import com.shadowcrypt.exception.KeyStorageException;
import com.shadowcrypt.util.Bytes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

/**
 * PEM-like text encoding of an RSA public key (X.509 SubjectPublicKeyInfo, base64)
 * and the SHA-256 fingerprint used to identify it.
 */
public final class PublicKeyCodec {

    public static final String BEGIN_MARKER = "-----BEGIN SHADOWCRYPT PUBLIC KEY-----";
    public static final String END_MARKER = "-----END SHADOWCRYPT PUBLIC KEY-----";
    public static final String FILE_EXTENSION = ".scpub";

    private static final int BASE64_LINE_LENGTH = 64;

    private PublicKeyCodec() {
    }

    public static String encode(PublicKey publicKey) {
        Objects.requireNonNull(publicKey, "publicKey");
        byte[] encoded = publicKey.getEncoded();
        if (encoded == null) {
            throw new KeyStorageException("The public key does not expose its encoded form");
        }
        String base64 = Base64.getMimeEncoder(BASE64_LINE_LENGTH, new byte[]{'\n'}).encodeToString(encoded);
        return BEGIN_MARKER + "\n" + base64 + "\n" + END_MARKER + "\n";
    }

    public static PublicKey decode(String text) {
        Objects.requireNonNull(text, "text");
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n').trim();
        int begin = normalized.indexOf(BEGIN_MARKER);
        int end = normalized.indexOf(END_MARKER);
        if (begin < 0 || end < 0 || end <= begin) {
            throw new KeyStorageException("The public-key file is missing its header or footer");
        }
        String body = normalized.substring(begin + BEGIN_MARKER.length(), end).replaceAll("\\s", "");
        if (body.isEmpty()) {
            throw new KeyStorageException("The public-key file contains no key data");
        }

        byte[] der;
        try {
            der = Base64.getDecoder().decode(body);
        } catch (IllegalArgumentException e) {
            throw new KeyStorageException("The public-key data is not valid base64", e);
        }

        try {
            KeyFactory factory = KeyFactory.getInstance(CryptoConstants.RSA_ALGORITHM);
            PublicKey publicKey = factory.generatePublic(new X509EncodedKeySpec(der));
            if (!CryptoConstants.RSA_ALGORITHM.equalsIgnoreCase(publicKey.getAlgorithm())) {
                throw new KeyStorageException("The imported key is not an RSA public key");
            }
            return publicKey;
        } catch (GeneralSecurityException e) {
            throw new KeyStorageException("The imported key is not a valid RSA public key", e);
        }
    }

    public static String fingerprint(PublicKey publicKey) {
        Objects.requireNonNull(publicKey, "publicKey");
        byte[] encoded = publicKey.getEncoded();
        if (encoded == null) {
            throw new KeyStorageException("The public key does not expose its encoded form");
        }
        return Bytes.toColonHex(Sha256.hash(encoded));
    }

    public static void write(Path file, PublicKey publicKey, boolean replaceExisting) {
        Objects.requireNonNull(file, "file");
        Path absolute = file.toAbsolutePath();
        try {
            Path parent = absolute.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (!replaceExisting && Files.exists(absolute)) {
                throw new KeyStorageException("A key file already exists at " + absolute);
            }
            Files.writeString(absolute, encode(publicKey), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new KeyStorageException("Unable to write the public-key file: " + absolute, e);
        }
    }

    public static PublicKey read(Path file) {
        Objects.requireNonNull(file, "file");
        try {
            return decode(Files.readString(file, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new KeyStorageException("Unable to read the public-key file: " + file, e);
        }
    }
}
