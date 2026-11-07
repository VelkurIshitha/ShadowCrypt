package com.shadowcrypt.crypto;

import com.shadowcrypt.crypto.hashing.Sha256;
import com.shadowcrypt.crypto.keystorage.PublicKeyCodec;
import com.shadowcrypt.exception.KeyStorageException;
import com.shadowcrypt.support.CryptoFixtures;
import com.shadowcrypt.util.Bytes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicKeyCodecTest {

    @TempDir
    Path tempDirectory;

    @Test
    void encodesAndDecodesPublicKey() {
        PublicKey publicKey = CryptoFixtures.rsaKeyPair().getPublic();

        String encoded = PublicKeyCodec.encode(publicKey);
        PublicKey decoded = PublicKeyCodec.decode(encoded);

        assertTrue(encoded.startsWith(PublicKeyCodec.BEGIN_MARKER));
        assertTrue(encoded.endsWith(PublicKeyCodec.END_MARKER + "\n"));
        assertArrayEquals(publicKey.getEncoded(), decoded.getEncoded());
    }

    @Test
    void fingerprintIsSha256OfEncodedKey() {
        PublicKey publicKey = CryptoFixtures.rsaKeyPair().getPublic();

        String expected = Bytes.toColonHex(Sha256.hash(publicKey.getEncoded()));
        String fingerprint = PublicKeyCodec.fingerprint(publicKey);

        assertEquals(expected, fingerprint);
        assertEquals(95, fingerprint.length());
    }

    @Test
    void rejectsGarbageText() {
        assertThrows(KeyStorageException.class, () -> PublicKeyCodec.decode("not a key"));
        assertThrows(KeyStorageException.class, () -> PublicKeyCodec.decode(
                PublicKeyCodec.BEGIN_MARKER + "\n!!!!\n" + PublicKeyCodec.END_MARKER));
    }

    @Test
    void writesAndReadsKeyFile() {
        PublicKey publicKey = CryptoFixtures.rsaKeyPair().getPublic();
        Path file = tempDirectory.resolve("recipient.scpub");

        PublicKeyCodec.write(file, publicKey, false);
        PublicKey read = PublicKeyCodec.read(file);

        assertArrayEquals(publicKey.getEncoded(), read.getEncoded());
        assertThrows(KeyStorageException.class, () -> PublicKeyCodec.write(file, publicKey, false));
    }
}
