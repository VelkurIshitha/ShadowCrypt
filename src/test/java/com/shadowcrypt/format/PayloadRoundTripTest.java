package com.shadowcrypt.format;

import com.shadowcrypt.exception.MalformedPackageException;
import com.shadowcrypt.format.payload.InnerPayload;
import com.shadowcrypt.format.payload.PayloadParser;
import com.shadowcrypt.format.payload.PayloadSerializer;
import com.shadowcrypt.model.PayloadType;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PayloadRoundTripTest {

    @Test
    void roundTripsTextPayload() {
        InnerPayload payload = new InnerPayload(PayloadType.TEXT, "",
                "text/plain; charset=utf-8", "Hello, ShadowCrypt!".getBytes(StandardCharsets.UTF_8));

        InnerPayload parsed = PayloadParser.parse(PayloadSerializer.serialize(payload));

        assertEquals(PayloadType.TEXT, parsed.type());
        assertEquals("", parsed.fileName());
        assertEquals("text/plain; charset=utf-8", parsed.mimeType());
        assertArrayEquals(payload.data(), parsed.data());
    }

    @Test
    void roundTripsFilePayloadWithUnicodeName() {
        byte[] data = new byte[512];
        new Random(9L).nextBytes(data);
        InnerPayload payload = new InnerPayload(PayloadType.FILE, "résumé-日本語.pdf",
                "application/pdf", data);

        InnerPayload parsed = PayloadParser.parse(PayloadSerializer.serialize(payload));

        assertEquals(PayloadType.FILE, parsed.type());
        assertEquals("résumé-日本語.pdf", parsed.fileName());
        assertEquals("application/pdf", parsed.mimeType());
        assertArrayEquals(data, parsed.data());
    }

    @Test
    void roundTripsEmptyAndSingleByteData() {
        InnerPayload empty = new InnerPayload(PayloadType.FILE, "empty.bin",
                "application/octet-stream", new byte[0]);
        InnerPayload single = new InnerPayload(PayloadType.FILE, "one.bin",
                "application/octet-stream", new byte[]{0x00});

        assertArrayEquals(new byte[0], PayloadParser.parse(PayloadSerializer.serialize(empty)).data());
        assertArrayEquals(new byte[]{0x00}, PayloadParser.parse(PayloadSerializer.serialize(single)).data());
    }

    @Test
    void lengthFieldsCountUtf8Bytes() {
        InnerPayload payload = new InnerPayload(PayloadType.FILE, "é",
                "m", new byte[0]);

        byte[] serialized = PayloadSerializer.serialize(payload);
        int fileNameLength = ByteBuffer.wrap(serialized).getShort(6);

        assertEquals(2, fileNameLength);
    }

    @Test
    void rejectsBadInnerMagic() {
        byte[] serialized = serializedText();
        serialized[0] = 0x58;

        assertRejected(serialized);
    }

    @Test
    void rejectsUnsupportedInnerVersion() {
        byte[] serialized = serializedText();
        serialized[4] = 2;

        assertRejected(serialized);
    }

    @Test
    void rejectsUnknownPayloadType() {
        byte[] serialized = serializedText();
        serialized[5] = 3;

        assertRejected(serialized);
    }

    @Test
    void rejectsTruncatedPayload() {
        byte[] serialized = serializedText();
        assertRejected(Arrays.copyOf(serialized, serialized.length - 1));
    }

    @Test
    void rejectsTrailingBytes() {
        byte[] serialized = serializedText();
        assertRejected(Arrays.copyOf(serialized, serialized.length + 1));
    }

    @Test
    void rejectsPathSeparatorsInSerializedFileName() {
        byte[] serialized = PayloadSerializer.serialize(new InnerPayload(
                PayloadType.FILE, "a_b", "application/octet-stream", new byte[0]));
        serialized[8] = '/';

        assertRejected(serialized);
    }

    @Test
    void serializerRejectsPathSeparators() {
        assertThrows(MalformedPackageException.class, () -> PayloadSerializer.serialize(
                new InnerPayload(PayloadType.FILE, "../escape.txt", "text/plain", new byte[0])));
    }

    private static void assertRejected(byte[] bytes) {
        assertThrows(MalformedPackageException.class, () -> PayloadParser.parse(bytes));
    }

    private static byte[] serializedText() {
        return PayloadSerializer.serialize(new InnerPayload(
                PayloadType.TEXT, "", "text/plain", "payload".getBytes(StandardCharsets.UTF_8)));
    }
}
