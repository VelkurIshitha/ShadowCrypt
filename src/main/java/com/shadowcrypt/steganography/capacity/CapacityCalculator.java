package com.shadowcrypt.steganography.capacity;

import com.shadowcrypt.config.AppConstants;
import com.shadowcrypt.exception.CapacityExceededException;

/** LSB capacity math: one payload bit per RGB channel. */
public final class CapacityCalculator {

    private CapacityCalculator() {
    }

    /** Theoretical capacity in bytes: {@code floor(width * height * 3 / 8)}. */
    public static long capacityBytes(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Image dimensions must be positive");
        }
        return ((long) width * (long) height * AppConstants.CHANNELS_PER_PIXEL) / 8;
    }

    public static void requireCapacity(long requiredBytes, int width, int height) {
        long capacity = capacityBytes(width, height);
        if (requiredBytes < 0) {
            throw new IllegalArgumentException("Required capacity must not be negative");
        }
        if (requiredBytes > capacity) {
            throw new CapacityExceededException(requiredBytes, capacity);
        }
    }
}
