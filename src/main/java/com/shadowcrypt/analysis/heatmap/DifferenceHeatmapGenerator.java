package com.shadowcrypt.analysis.heatmap;

import com.shadowcrypt.analysis.comparison.ImageComparison;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Builds an amplified per-channel absolute-difference image so single-LSB changes
 * are visible to the eye.
 */
public final class DifferenceHeatmapGenerator {

    private DifferenceHeatmapGenerator() {
    }

    public static BufferedImage generate(BufferedImage original, BufferedImage stego, int amplification) {
        ImageComparison.requireSameSize(original, stego);
        if (amplification < 1) {
            throw new IllegalArgumentException("The heatmap amplification must be at least 1");
        }

        int width = original.getWidth();
        int height = original.getHeight();
        int[] originalPixels = ImageComparison.pixelRgb(original);
        int[] stegoPixels = ImageComparison.pixelRgb(stego);
        int[] heatmapPixels = new int[originalPixels.length];

        for (int pixel = 0; pixel < originalPixels.length; pixel++) {
            int heatPixel = 0xFF000000;
            for (int shift = 16; shift >= 0; shift -= 8) {
                int difference = Math.abs(
                        ((originalPixels[pixel] >>> shift) & 0xFF)
                                - ((stegoPixels[pixel] >>> shift) & 0xFF));
                int amplified = Math.min(255, difference * amplification);
                heatPixel |= amplified << shift;
            }
            heatmapPixels[pixel] = heatPixel;
        }

        BufferedImage heatmap = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        heatmap.setRGB(0, 0, width, height, heatmapPixels, 0, width);
        return heatmap;
    }

    public static BufferedImage generate(BufferedImage original, BufferedImage stego) {
        Objects.requireNonNull(original, "original");
        return generate(original, stego, 16);
    }
}
