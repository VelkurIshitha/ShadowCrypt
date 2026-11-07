package com.shadowcrypt.steganography.encoding;

import com.shadowcrypt.exception.CapacityExceededException;
import com.shadowcrypt.steganography.capacity.CapacityCalculator;
import com.shadowcrypt.steganography.lsb.LsbChannelCursor;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Embeds bytes into the least significant bit of each RGB channel. Bits are written
 * most-significant-bit first, one bit per channel, in the shared cursor order. The
 * carrier is not modified; a new opaque RGB image is returned.
 */
public final class LsbEncoder {

    private LsbEncoder() {
    }

    public static BufferedImage embed(BufferedImage normalizedCarrier, byte[] payload) {
        Objects.requireNonNull(normalizedCarrier, "normalizedCarrier");
        Objects.requireNonNull(payload, "payload");
        requireNormalizedRgb(normalizedCarrier);

        int width = normalizedCarrier.getWidth();
        int height = normalizedCarrier.getHeight();
        long capacity = CapacityCalculator.capacityBytes(width, height);
        if (payload.length > capacity) {
            throw new CapacityExceededException(payload.length, capacity);
        }

        int[] pixels = normalizedCarrier.getRGB(0, 0, width, height, null, 0, width);
        LsbChannelCursor cursor = new LsbChannelCursor(width, height);

        for (byte payloadByte : payload) {
            int value = payloadByte & 0xFF;
            for (int bit = 7; bit >= 0; bit--) {
                long channelIndex = cursor.nextChannelIndex();
                int pixelIndex = LsbChannelCursor.pixelIndex(channelIndex);
                int shift = LsbChannelCursor.channelShift(channelIndex);
                int bitValue = (value >>> bit) & 1;
                pixels[pixelIndex] = (pixels[pixelIndex] & ~(1 << shift)) | (bitValue << shift);
            }
        }

        BufferedImage stego = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        stego.setRGB(0, 0, width, height, pixels, 0, width);
        return stego;
    }

    private static void requireNormalizedRgb(BufferedImage image) {
        if (image.getType() != BufferedImage.TYPE_INT_RGB) {
            throw new IllegalArgumentException(
                    "The carrier must be normalized to TYPE_INT_RGB before embedding");
        }
    }
}
