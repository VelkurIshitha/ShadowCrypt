package com.shadowcrypt.steganography;

import com.shadowcrypt.exception.CapacityExceededException;
import com.shadowcrypt.image.normalization.CarrierNormalizer;
import com.shadowcrypt.image.processing.PngWriter;
import com.shadowcrypt.steganography.capacity.CapacityCalculator;
import com.shadowcrypt.steganography.encoding.LsbEncoder;
import com.shadowcrypt.steganography.extraction.LsbDecoder;
import com.shadowcrypt.support.TestCarrierFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LsbRoundTripTest {

    @TempDir
    Path tempDirectory;

    @Test
    void roundTripsRandomBinaryData() {
        BufferedImage carrier = TestCarrierFactory.randomCarrier(120, 90, 1L);
        byte[] payload = new byte[3_000];
        new Random(2L).nextBytes(payload);

        byte[] recovered = LsbDecoder.extract(LsbEncoder.embed(carrier, payload), payload.length);

        assertArrayEquals(payload, recovered);
    }

    @Test
    void roundTripsEveryByteValueIncludingHighBitSet() {
        BufferedImage carrier = TestCarrierFactory.randomCarrier(64, 64, 3L);
        byte[] payload = new byte[1_024];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) (i & 0xFF);
        }

        byte[] recovered = LsbDecoder.extract(LsbEncoder.embed(carrier, payload), payload.length);

        assertArrayEquals(payload, recovered);
    }

    @Test
    void emptyPayloadLeavesCarrierBytesUntouched() {
        BufferedImage carrier = TestCarrierFactory.randomCarrier(32, 32, 4L);

        BufferedImage stego = LsbEncoder.embed(carrier, new byte[0]);

        assertArrayEquals(rgb(carrier), rgb(stego));
        assertEquals(0, LsbDecoder.extract(stego, 0).length);
    }

    @Test
    void onlyLeastSignificantBitsChange() {
        BufferedImage carrier = TestCarrierFactory.randomCarrier(80, 60, 5L);
        byte[] payload = new byte[500];
        new Random(6L).nextBytes(payload);

        BufferedImage stego = LsbEncoder.embed(carrier, payload);

        int[] before = rgb(carrier);
        int[] after = rgb(stego);
        for (int pixel = 0; pixel < before.length; pixel++) {
            for (int shift = 16; shift >= 0; shift -= 8) {
                int originalChannel = (before[pixel] >>> shift) & 0xFF;
                int stegoChannel = (after[pixel] >>> shift) & 0xFF;
                assertEquals(originalChannel & 0xFE, stegoChannel & 0xFE);
            }
        }
    }

    @Test
    void rejectsPayloadLargerThanCapacity() {
        BufferedImage carrier = TestCarrierFactory.randomCarrier(10, 10, 7L);
        int capacity = (int) CapacityCalculator.capacityBytes(10, 10);

        assertThrows(CapacityExceededException.class,
                () -> LsbEncoder.embed(carrier, new byte[capacity + 1]));
    }

    @Test
    void acceptsPayloadExactlyAtCapacity() {
        int payloadLength = 500;
        BufferedImage carrier = TestCarrierFactory.carrierWithCapacityExactly(payloadLength);
        byte[] payload = new byte[payloadLength];
        new Random(8L).nextBytes(payload);

        byte[] recovered = LsbDecoder.extract(LsbEncoder.embed(carrier, payload), payloadLength);

        assertArrayEquals(payload, recovered);
    }

    @Test
    void rejectsExtractionBeyondCapacity() {
        BufferedImage image = TestCarrierFactory.randomCarrier(4, 4, 9L);
        int capacity = (int) CapacityCalculator.capacityBytes(4, 4);

        assertThrows(CapacityExceededException.class, () -> LsbDecoder.extract(image, capacity + 1));
    }

    @Test
    void survivesPngWriteAndReload() {
        BufferedImage carrier = TestCarrierFactory.randomCarrier(64, 48, 10L);
        byte[] payload = new byte[900];
        new Random(11L).nextBytes(payload);

        Path stegoFile = tempDirectory.resolve("stego.png");
        PngWriter.write(LsbEncoder.embed(carrier, payload), stegoFile, false);
        BufferedImage reloaded = CarrierNormalizer.load(stegoFile).image();

        assertArrayEquals(payload, LsbDecoder.extract(reloaded, payload.length));
    }

    @Test
    void rejectsNonNormalizedCarrier() {
        BufferedImage argb = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

        assertThrows(IllegalArgumentException.class,
                () -> LsbEncoder.embed(argb, new byte[1]));
        assertThrows(IllegalArgumentException.class,
                () -> LsbDecoder.extract(argb, 1));
    }

    private static int[] rgb(BufferedImage image) {
        return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
    }
}
