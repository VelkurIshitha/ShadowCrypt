package com.shadowcrypt.crypto;

import com.shadowcrypt.crypto.hashing.Sha256;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Sha256Test {

    @Test
    void matchesKnownVectorForAbc() {
        byte[] digest = Sha256.hash("abc".getBytes(StandardCharsets.US_ASCII));

        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                hex(digest));
    }

    @Test
    void matchesKnownVectorForEmptyInput() {
        byte[] digest = Sha256.hash(new byte[0]);

        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                hex(digest));
    }

    @Test
    void fileHashMatchesByteHash(@TempDir Path directory) throws Exception {
        Path file = directory.resolve("sample.bin");
        byte[] content = new byte[10_000];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (i * 31);
        }
        Files.write(file, content);

        assertArrayEquals(Sha256.hash(content), Sha256.hash(file));
        assertEquals(Sha256.hex(content), Sha256.hex(file));
    }

    @Test
    void comparisonDetectsEqualityAndDifference() {
        byte[] first = Sha256.hash("first".getBytes(StandardCharsets.UTF_8));
        byte[] second = Sha256.hash("second".getBytes(StandardCharsets.UTF_8));

        assertTrue(Sha256.matches(first, first.clone()));
        assertFalse(Sha256.matches(first, second));
    }

    private static String hex(byte[] digest) {
        return com.shadowcrypt.util.Bytes.toHex(digest);
    }
}
