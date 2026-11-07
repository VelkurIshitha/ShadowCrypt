package com.shadowcrypt.crypto.keystorage;

import com.shadowcrypt.config.CryptoConstants;
import com.shadowcrypt.crypto.aes.AesGcmCipher;
import com.shadowcrypt.crypto.hashing.Sha256;
import com.shadowcrypt.crypto.keyderivation.Pbkdf2KeyDerivation;
import com.shadowcrypt.exception.AuthenticationFailedException;
import com.shadowcrypt.exception.KeyStorageException;
import com.shadowcrypt.util.SecureWipe;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Objects;

/**
 * Writes and reads passphrase-protected private keys. The PKCS#8 key is encrypted
 * with AES-256-GCM using a PBKDF2-HMAC-SHA256 key; salt, nonce, and iteration count
 * are stored in the file and authenticated as additional data.
 */
public final class PrivateKeyStore {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PrivateKeyStore() {
    }

    public static void save(Path file, PrivateKey privateKey, char[] passphrase, boolean replaceExisting) {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(privateKey, "privateKey");
        requirePassphrase(passphrase);

        byte[] pkcs8 = privateKey.getEncoded();
        if (pkcs8 == null) {
            throw new KeyStorageException("The private key does not expose its encoded form");
        }

        byte[] salt = new byte[CryptoConstants.PBKDF2_SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] nonce = new byte[CryptoConstants.GCM_NONCE_BYTES];
        RANDOM.nextBytes(nonce);
        int iterations = CryptoConstants.PBKDF2_ITERATIONS;

        try {
            SecretKey derivedKey = Pbkdf2KeyDerivation.deriveKey(passphrase, salt, iterations);
            byte[] header = PrivateKeyFileFormat.headerBytes(
                    iterations, salt, nonce, pkcs8.length + CryptoConstants.GCM_TAG_BYTES);
            byte[] ciphertext = AesGcmCipher.encrypt(derivedKey, nonce, pkcs8, header);

            PrivateKeyFileFormat.Container container =
                    new PrivateKeyFileFormat.Container(iterations, salt, nonce, ciphertext);
            writeAtomically(file, PrivateKeyFileFormat.serialize(container), replaceExisting);
        } finally {
            SecureWipe.wipe(pkcs8);
        }
    }

    public static PrivateKey load(Path file, char[] passphrase) {
        Objects.requireNonNull(file, "file");
        requirePassphrase(passphrase);

        byte[] fileBytes = readContainerBytes(file);
        PrivateKeyFileFormat.Container container = PrivateKeyFileFormat.parse(fileBytes);

        byte[] header = PrivateKeyFileFormat.headerBytes(
                container.iterations(), container.salt(), container.nonce(), container.ciphertext().length);

        byte[] pkcs8 = null;
        try {
            SecretKey derivedKey = Pbkdf2KeyDerivation.deriveKey(
                    passphrase, container.salt(), container.iterations());
            pkcs8 = AesGcmCipher.decrypt(derivedKey, container.nonce(), container.ciphertext(), header);

            KeyFactory factory = KeyFactory.getInstance(CryptoConstants.RSA_ALGORITHM);
            PrivateKey privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(pkcs8));
            if (!CryptoConstants.RSA_ALGORITHM.equalsIgnoreCase(privateKey.getAlgorithm())) {
                throw new KeyStorageException("The stored key is not an RSA private key");
            }
            return privateKey;
        } catch (AuthenticationFailedException e) {
            throw new KeyStorageException("Incorrect passphrase or corrupted private-key file", e);
        } catch (GeneralSecurityException e) {
            throw new KeyStorageException("Unable to read the private key", e);
        } finally {
            SecureWipe.wipe(pkcs8);
        }
    }

    /** Rebuilds the public half so its fingerprint can be displayed. */
    public static PublicKey derivePublicKey(PrivateKey privateKey) {
        Objects.requireNonNull(privateKey, "privateKey");
        if (!(privateKey instanceof RSAPrivateCrtKey crtKey)) {
            throw new KeyStorageException("The private key does not contain its public exponent");
        }
        try {
            KeyFactory factory = KeyFactory.getInstance(CryptoConstants.RSA_ALGORITHM);
            return factory.generatePublic(new RSAPublicKeySpec(crtKey.getModulus(), crtKey.getPublicExponent()));
        } catch (GeneralSecurityException e) {
            throw new KeyStorageException("Unable to derive the public key", e);
        }
    }

    public static String fingerprint(PrivateKey privateKey) {
        PublicKey publicKey = derivePublicKey(privateKey);
        byte[] encoded = publicKey.getEncoded();
        if (encoded == null) {
            throw new KeyStorageException("The public key does not expose its encoded form");
        }
        return com.shadowcrypt.util.Bytes.toColonHex(Sha256.hash(encoded));
    }

    private static byte[] readContainerBytes(Path file) {
        try {
            if (!Files.isRegularFile(file)) {
                throw new KeyStorageException("The private-key file does not exist: " + file);
            }
            long size = Files.size(file);
            if (size <= 0 || size > PrivateKeyFileFormat.MAX_CONTAINER_BYTES) {
                throw new KeyStorageException("The private-key file has an invalid size: " + size + " bytes");
            }
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new KeyStorageException("Unable to read the private-key file: " + file, e);
        }
    }

    private static void writeAtomically(Path file, byte[] data, boolean replaceExisting) {
        Path absolute = file.toAbsolutePath();
        Path parent = absolute.getParent();
        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (!replaceExisting && Files.exists(absolute)) {
                throw new KeyStorageException("A key file already exists at " + absolute);
            }
            Path temp = Files.createTempFile(parent != null ? parent : Path.of("."), ".shadowcrypt-key", ".tmp");
            try {
                Files.write(temp, data);
                try {
                    Files.move(temp, absolute,
                            StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(temp);
            }
        } catch (IOException e) {
            throw new KeyStorageException("Unable to write the private-key file: " + absolute, e);
        }
    }

    private static void requirePassphrase(char[] passphrase) {
        Objects.requireNonNull(passphrase, "passphrase");
        if (passphrase.length == 0) {
            throw new KeyStorageException("The passphrase must not be empty");
        }
    }
}
