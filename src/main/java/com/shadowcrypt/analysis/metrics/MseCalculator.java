package com.shadowcrypt.analysis.metrics;

import com.shadowcrypt.analysis.comparison.ImageComparison;

import java.awt.image.BufferedImage;

/** Mean squared error over corresponding RGB channels. */
public final class MseCalculator {

    private MseCalculator() {
    }

    public static double calculate(BufferedImage original, BufferedImage stego) {
        long channels = ImageComparison.channelCount(original);
        if (channels == 0) {
            throw new IllegalArgumentException("The images contain no channel samples");
        }
        return (double) ImageComparison.squaredErrorSum(original, stego) / channels;
    }
}
