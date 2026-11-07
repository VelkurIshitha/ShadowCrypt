package com.shadowcrypt.crypto;

import com.shadowcrypt.crypto.aes.AesGcmCipher;
import com.shadowcrypt.crypto.aes.AesSessionKey;
import com.shadowcrypt.exception.AuthenticationFailedException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AesGcmCipherTest {

    private static final byte[] AAD = "canonical-header-bytes".getBytes(StandardCharsets.UTF_8);

    @Test
    void encryptsAndDecrypts() {
        AesSessionKey session = AesSessionKey.generate();
        byte[] plaintext = "ShadowCrypt round trip".getBytes(StandardCharsets.UTF_8);

        byte[] ciphertext = AesGcmCipher.encrypt(session.key(), session.nonce(), plaintext, AAD);
        byte[] recovered = AesGcmCipher.decrypt(session.key(), session.nonce(), ciphertext, AAD);

        assertArrayEquals(plaintext, recovered);
        assertEquals(plaintext.length + 16, ciphertext.length);
    }

    @Test
    void rejectsTamperedCiphertext() {
        AesSessionKey session = AesSessionKey.generate();
        byte[] plaintext = "tamper me".getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = AesGcmCipher.encrypt(session.key(), session.nonce(), plaintext, AAD);

        ciphertext[0] ^= 0x01;

        assertThrows(AuthenticationFailedException.class,
                () -> AesGcmCipher.decrypt(session.key(), session.nonce(), ciphertext, AAD));
    }

    @Test
    void rejectsDifferentAdditionalData() {
        AesSessionKey session = AesSessionKey.generate();
        byte[] plaintext = "aad check".getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = AesGcmCipher.encrypt(session.key(), session.nonce(), plaintext, AAD);

        byte[] otherAad = "different-header".getBytes(StandardCharsets.UTF_8);

        assertThrows(AuthenticationFailedException.class,
                () -> AesGcmCipher.decrypt(session.key(), session.nonce(), ciphertext, otherAad));
    }

    @Test
    void sessionKeysUseFreshNonces() {
        AesSessionKey first = AesSessionKey.generate();
        AesSessionKey second = AesSessionKey.generate();

        assertEquals(12, first.nonce().length);
        assertFalse(java.util.Arrays.equals(first.nonce(), second.nonce()));
    }

    @Test
    void rejectsNonceOfWrongLength() {
        AesSessionKey session = AesSessionKey.generate();
        byte[] shortNonce = new byte[8];

        assertThrows(IllegalArgumentException.class,
                () -> AesGcmCipher.encrypt(session.key(), shortNonce,
                        "x".getBytes(StandardCharsets.UTF_8), AAD));
    }

    @Test
    void rejectsCiphertextShorterThanTag() {
        AesSessionKey session = AesSessionKey.generate();

        assertThrows(AuthenticationFailedException.class,
                () -> AesGcmCipher.decrypt(session.key(), session.nonce(), new byte[8], AAD));
    }
}
