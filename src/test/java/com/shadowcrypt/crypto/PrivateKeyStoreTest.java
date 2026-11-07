package com.shadowcrypt.crypto;

import com.shadowcrypt.config.CryptoConstants;
import com.shadowcrypt.crypto.keystorage.PrivateKeyFileFormat;
import com.shadowcrypt.crypto.keystorage.PrivateKeyStore;
import com.shadowcrypt.exception.KeyStorageException;
import com.shadowcrypt.support.CryptoFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PrivateKey;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrivateKeyStoreTest {

    private static final char[] PASSPHRASE = "shadowcrypt-test-passphrase".toCharArray();

    @TempDir
    Path tempDirectory;

    @Test
    void savesAndLoadsProtectedPrivateKey() {
        KeyPair pair = CryptoFixtures.rsaKeyPair();
        Path file = tempDirectory.resolve("alice.scpriv");

        PrivateKeyStore.save(file, pair.getPrivate(), PASSPHRASE, false);
        PrivateKey loaded = PrivateKeyStore.load(file, PASSPHRASE);

        assertTrue(Files.exists(file));
        assertArrayEquals(pair.getPrivate().getEncoded(), loaded.getEncoded());
        assertArrayEquals(pair.getPublic().getEncoded(),
                PrivateKeyStore.derivePublicKey(loaded).getEncoded());
    }

    @Test
    void storesConfiguredIterationCount() throws IOException {
        Path file = tempDirectory.resolve("iterations.scpriv");
        PrivateKeyStore.save(file, CryptoFixtures.rsaKeyPair().getPrivate(), PASSPHRASE, false);

        PrivateKeyFileFormat.Container container =
                PrivateKeyFileFormat.parse(Files.readAllBytes(file));

        assertEquals(CryptoConstants.PBKDF2_ITERATIONS, container.iterations());
    }

    @Test
    void rejectsIncorrectPassphrase() {
        Path file = tempDirectory.resolve("wrong-pass.scpriv");
        PrivateKeyStore.save(file, CryptoFixtures.rsaKeyPair().getPrivate(), PASSPHRASE, false);

        KeyStorageException exception = assertThrows(KeyStorageException.class,
                () -> PrivateKeyStore.load(file, "not-the-passphrase".toCharArray()));

        assertTrue(exception.getMessage().contains("passphrase"));
    }

    @Test
    void refusesToOverwriteWithoutPermission() {
        Path file = tempDirectory.resolve("overwrite.scpriv");
        PrivateKey pair = CryptoFixtures.rsaKeyPair().getPrivate();
        PrivateKeyStore.save(file, pair, PASSPHRASE, false);

        assertThrows(KeyStorageException.class,
                () -> PrivateKeyStore.save(file, pair, PASSPHRASE, false));
    }

    @Test
    void overwritesWhenPermissionIsGiven() {
        Path file = tempDirectory.resolve("replace.scpriv");
        PrivateKey pair = CryptoFixtures.rsaKeyPair().getPrivate();
        PrivateKeyStore.save(file, pair, PASSPHRASE, false);

        PrivateKeyStore.save(file, pair, PASSPHRASE, true);

        assertArrayEquals(pair.getEncoded(), PrivateKeyStore.load(file, PASSPHRASE).getEncoded());
    }

    @Test
    void rejectsTruncatedFile() throws IOException {
        Path file = tempDirectory.resolve("truncated.scpriv");
        PrivateKeyStore.save(file, CryptoFixtures.rsaKeyPair().getPrivate(), PASSPHRASE, false);
        byte[] bytes = Files.readAllBytes(file);
        Files.write(file, java.util.Arrays.copyOf(bytes, 20));

        assertThrows(KeyStorageException.class, () -> PrivateKeyStore.load(file, PASSPHRASE));
    }

    @Test
    void rejectsCorruptedCiphertext() throws IOException {
        Path file = tempDirectory.resolve("corrupted.scpriv");
        PrivateKeyStore.save(file, CryptoFixtures.rsaKeyPair().getPrivate(), PASSPHRASE, false);
        byte[] bytes = Files.readAllBytes(file);
        bytes[bytes.length - 1] ^= 0x01;
        Files.write(file, bytes);

        assertThrows(KeyStorageException.class, () -> PrivateKeyStore.load(file, PASSPHRASE));
    }
}
