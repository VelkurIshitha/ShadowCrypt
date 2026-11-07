package com.shadowcrypt.steganography.extraction;

import com.shadowcrypt.exception.CapacityExceededException;
import com.shadowcrypt.steganography.capacity.CapacityCalculator;
import com.shadowcrypt.steganography.lsb.LsbChannelCursor;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Reads payload bytes from the RGB LSBs using exactly the same traversal as the
 * encoder. Callers supply a byte count so extraction can be bounded before any
 * buffers are allocated.
 */
public final class LsbDecoder {

    private LsbDecoder() {
    }

    public static byte[] extract(BufferedImage normalizedStego, int byteCount) {
        Objects.requireNonNull(normalizedStego, "normalizedStego");
        if (byteCount < 0) {
            throw new IllegalArgumentException("Byte count must not be negative");
        }
        requireNormalizedRgb(normalizedStego);

        int width = normalizedStego.getWidth();
        int height = normalizedStego.getHeight();
        long capacity = CapacityCalculator.capacityBytes(width, height);
        if (byteCount > capacity) {
            throw new CapacityExceededException(byteCount, capacity);
        }

        int[] pixels = normalizedStego.getRGB(0, 0, width, height, null, 0, width);
        LsbChannelCursor cursor = new LsbChannelCursor(width, height);
        byte[] result = new byte[byteCount];

        for (int i = 0; i < byteCount; i++) {
            int value = 0;
            for (int bit = 0; bit < 8; bit++) {
                long channelIndex = cursor.nextChannelIndex();
                int pixelIndex = LsbChannelCursor.pixelIndex(channelIndex);
                int shift = LsbChannelCursor.channelShift(channelIndex);
                value = (value << 1) | ((pixels[pixelIndex] >>> shift) & 1);
            }
            result[i] = (byte) value;
        }
        return result;
    }

    private static void requireNormalizedRgb(BufferedImage image) {
        if (image.getType() != BufferedImage.TYPE_INT_RGB) {
            throw new IllegalArgumentException(
                    "The stego image must be normalized to TYPE_INT_RGB before extraction");
        }
    }
}
