package com.shadowcrypt.steganography.lsb;

import com.shadowcrypt.config.AppConstants;
import com.shadowcrypt.exception.SteganographyException;

/**
 * Deterministic traversal shared by the encoder and decoder: row-major, left to
 * right, top to bottom, and within each pixel red, green, blue. A single counter
 * over RGB channels is used so the two sides cannot drift apart.
 */
public final class LsbChannelCursor {

    private final long totalChannels;
    private long channelIndex;

    public LsbChannelCursor(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Image dimensions must be positive");
        }
        this.totalChannels = (long) width * height * AppConstants.CHANNELS_PER_PIXEL;
    }

    public boolean hasNext() {
        return channelIndex < totalChannels;
    }

    /** Returns the next channel index and advances the cursor. */
    public long nextChannelIndex() {
        if (!hasNext()) {
            throw new SteganographyException("The carrier image has no more channel capacity");
        }
        return channelIndex++;
    }

    public long remainingChannels() {
        return totalChannels - channelIndex;
    }

    public static int pixelIndex(long channelIndex) {
        return (int) (channelIndex / AppConstants.CHANNELS_PER_PIXEL);
    }

    /** Bit position of the channel inside an ARGB pixel: red = 16, green = 8, blue = 0. */
    public static int channelShift(long channelIndex) {
        int channelInPixel = (int) (channelIndex % AppConstants.CHANNELS_PER_PIXEL);
        return 16 - 8 * channelInPixel;
    }
}
