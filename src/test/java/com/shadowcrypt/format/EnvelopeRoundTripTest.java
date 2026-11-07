package com.shadowcrypt.format;

import com.shadowcrypt.format.envelope.Envelope;
import com.shadowcrypt.format.envelope.EnvelopeParser;
import com.shadowcrypt.format.envelope.EnvelopeSerializer;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvelopeRoundTripTest {

    @Test
    void roundTripsAllEnvelopeFields() {
        Envelope original = sampleEnvelope(128);

        byte[] serialized = EnvelopeSerializer.serialize(original);
        Envelope parsed = EnvelopeParser.parse(serialized);

        assertEquals(original.flags(), parsed.flags());
        assertArrayEquals(original.wrappedKey(), parsed.wrappedKey());
        assertArrayEquals(original.nonce(), parsed.nonce());
        assertArrayEquals(original.ciphertext(), parsed.ciphertext());
        assertEquals(17 + 384 + 12 + 128, serialized.length);
        assertEquals(serialized.length, original.totalLength());
    }

    @Test
    void aadIsHeaderPlusWrappedKeyPlusNonce() {
        Envelope envelope = sampleEnvelope(64);

        byte[] serialized = EnvelopeSerializer.serialize(envelope);
        byte[] aad = EnvelopeSerializer.aadBytes(envelope);

        assertEquals(413, aad.length);
        assertArrayEquals(Arrays.copyOfRange(serialized, 0, 413), aad);
        assertArrayEquals(aad, EnvelopeParser.aadBytes(serialized));
    }

    @Test
    void parsedEnvelopeSerializesCanonically() {
        Envelope original = sampleEnvelope(33);
        byte[] serialized = EnvelopeSerializer.serialize(original);

        Envelope parsed = EnvelopeParser.parse(serialized);

        assertArrayEquals(serialized, EnvelopeSerializer.serialize(parsed));
    }

    private static Envelope sampleEnvelope(int ciphertextLength) {
        Random random = new Random(424242L);
        byte[] wrappedKey = new byte[384];
        byte[] nonce = new byte[12];
        byte[] ciphertext = new byte[ciphertextLength];
        random.nextBytes(wrappedKey);
        random.nextBytes(nonce);
        random.nextBytes(ciphertext);
        return new Envelope(0, wrappedKey, nonce, ciphertext);
    }
}
