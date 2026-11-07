package com.shadowcrypt.crypto;

import com.shadowcrypt.crypto.keyderivation.Pbkdf2KeyDerivation;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Pbkdf2KeyDerivationTest {

    private static final int FAST_ITERATIONS = 1_000;

    @Test
    void derivationIsDeterministic() {
        char[] passphrase = "correct horse battery staple".toCharArray();
        byte[] salt = "0123456789abcdef".getBytes(StandardCharsets.US_ASCII);

        SecretKey first = Pbkdf2KeyDerivation.deriveKey(passphrase, salt, FAST_ITERATIONS);
        SecretKey second = Pbkdf2KeyDerivation.deriveKey(passphrase, salt, FAST_ITERATIONS);

        assertArrayEquals(first.getEncoded(), second.getEncoded());
        assertFalse(java.util.Arrays.equals(first.getEncoded(), new byte[32]));
    }

    @Test
    void differentSaltProducesDifferentKey() {
        char[] passphrase = "passphrase".toCharArray();

        SecretKey first = Pbkdf2KeyDerivation.deriveKey(
                passphrase, "0123456789abcdef".getBytes(StandardCharsets.US_ASCII), FAST_ITERATIONS);
        SecretKey second = Pbkdf2KeyDerivation.deriveKey(
                passphrase, "fedcba9876543210".getBytes(StandardCharsets.US_ASCII), FAST_ITERATIONS);

        assertFalse(java.util.Arrays.equals(first.getEncoded(), second.getEncoded()));
    }

    @Test
    void differentPassphraseProducesDifferentKey() {
        byte[] salt = "0123456789abcdef".getBytes(StandardCharsets.US_ASCII);

        SecretKey first = Pbkdf2KeyDerivation.deriveKey("first".toCharArray(), salt, FAST_ITERATIONS);
        SecretKey second = Pbkdf2KeyDerivation.deriveKey("second".toCharArray(), salt, FAST_ITERATIONS);

        assertFalse(java.util.Arrays.equals(first.getEncoded(), second.getEncoded()));
    }

    @Test
    void rejectsEmptyPassphrase() {
        byte[] salt = "0123456789abcdef".getBytes(StandardCharsets.US_ASCII);

        assertThrows(IllegalArgumentException.class,
                () -> Pbkdf2KeyDerivation.deriveKey(new char[0], salt, FAST_ITERATIONS));
    }

    @Test
    void rejectsShortSalt() {
        assertThrows(IllegalArgumentException.class,
                () -> Pbkdf2KeyDerivation.deriveKey("pass".toCharArray(), new byte[4], FAST_ITERATIONS));
    }
}
