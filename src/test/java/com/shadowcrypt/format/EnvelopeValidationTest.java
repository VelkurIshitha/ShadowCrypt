package com.shadowcrypt.format;

import com.shadowcrypt.exception.MalformedPackageException;
import com.shadowcrypt.format.envelope.Envelope;
import com.shadowcrypt.format.envelope.EnvelopeParser;
import com.shadowcrypt.format.envelope.EnvelopeSerializer;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertThrows;

class EnvelopeValidationTest {

    private static final int VERSION_OFFSET = 4;
    private static final int FLAGS_OFFSET = 5;
    private static final int TOTAL_LENGTH_OFFSET = 6;
    private static final int WRAPPED_KEY_LENGTH_OFFSET = 10;
    private static final int NONCE_LENGTH_OFFSET = 12;
    private static final int CIPHERTEXT_LENGTH_OFFSET = 13;

    @Test
    void rejectsInvalidMagicNumber() {
        assertRejected(patched(serialized(), 0, 0x58));
    }

    @Test
    void rejectsUnsupportedVersion() {
        assertRejected(patched(serialized(), VERSION_OFFSET, 2));
    }

    @Test
    void rejectsUnknownFlags() {
        assertRejected(patched(serialized(), FLAGS_OFFSET, 0x01));
    }

    @Test
    void rejectsWrongWrappedKeyLength() {
        assertRejected(withShort(serialized(), WRAPPED_KEY_LENGTH_OFFSET, 383));
    }

    @Test
    void rejectsWrongNonceLength() {
        assertRejected(patched(serialized(), NONCE_LENGTH_OFFSET, 11));
    }

    @Test
    void rejectsCiphertextWithoutTag() {
        assertRejected(withInt(serialized(), CIPHERTEXT_LENGTH_OFFSET, 15));
    }

    @Test
    void rejectsTotalLengthMismatch() {
        byte[] bytes = serialized();
        int total = ByteBuffer.wrap(bytes).getInt(TOTAL_LENGTH_OFFSET);
        assertRejected(withInt(bytes, TOTAL_LENGTH_OFFSET, total + 1));
    }

    @Test
    void rejectsNegativeTotalLength() {
        assertRejected(withInt(serialized(), TOTAL_LENGTH_OFFSET, -1));
    }

    @Test
    void rejectsTruncatedHeader() {
        assertRejected(Arrays.copyOf(serialized(), 10));
    }

    @Test
    void rejectsEmptyInput() {
        assertRejected(new byte[0]);
    }

    @Test
    void rejectsTruncatedCiphertext() {
        byte[] bytes = serialized();
        assertRejected(Arrays.copyOf(bytes, bytes.length - 1));
    }

    @Test
    void rejectsTrailingBytes() {
        byte[] bytes = serialized();
        assertRejected(Arrays.copyOf(bytes, bytes.length + 1));
    }

    @Test
    void rejectsPackageLargerThanCarrierCapacity() {
        byte[] bytes = serialized();
        assertThrows(MalformedPackageException.class,
                () -> EnvelopeParser.parse(bytes, bytes.length - 1L));
    }

    private static void assertRejected(byte[] bytes) {
        assertThrows(MalformedPackageException.class, () -> EnvelopeParser.parse(bytes));
    }

    private static byte[] serialized() {
        Random random = new Random(7L);
        byte[] wrappedKey = new byte[384];
        byte[] nonce = new byte[12];
        byte[] ciphertext = new byte[48];
        random.nextBytes(wrappedKey);
        random.nextBytes(nonce);
        random.nextBytes(ciphertext);
        return EnvelopeSerializer.serialize(new Envelope(0, wrappedKey, nonce, ciphertext));
    }

    private static byte[] patched(byte[] source, int offset, int value) {
        byte[] copy = source.clone();
        copy[offset] = (byte) value;
        return copy;
    }

    private static byte[] withShort(byte[] source, int offset, int value) {
        ByteBuffer.wrap(source).putShort(offset, (short) value);
        return source;
    }

    private static byte[] withInt(byte[] source, int offset, int value) {
        ByteBuffer.wrap(source).putInt(offset, value);
        return source;
    }
}
