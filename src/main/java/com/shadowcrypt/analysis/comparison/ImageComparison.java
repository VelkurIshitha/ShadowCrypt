package com.shadowcrypt.analysis.comparison;

import com.shadowcrypt.config.AppConstants;

import java.awt.image.BufferedImage;
import java.util.Objects;

/** Safe shared iteration over two normalized RGB images. */
public final class ImageComparison {

    private ImageComparison() {
    }

    public static void requireSameSize(BufferedImage first, BufferedImage second) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        if (first.getWidth() != second.getWidth() || first.getHeight() != second.getHeight()) {
            throw new IllegalArgumentException("Images must have identical dimensions, got "
                    + first.getWidth() + "x" + first.getHeight() + " and "
                    + second.getWidth() + "x" + second.getHeight());
        }
    }

    public static int[] pixelRgb(BufferedImage image) {
        Objects.requireNonNull(image, "image");
        return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
    }

    public static long channelCount(BufferedImage image) {
        Objects.requireNonNull(image, "image");
        return (long) image.getWidth() * image.getHeight() * AppConstants.CHANNELS_PER_PIXEL;
    }

    /** Sum of squared RGB channel differences across both images. */
    public static long squaredErrorSum(BufferedImage first, BufferedImage second) {
        requireSameSize(first, second);
        int[] firstPixels = pixelRgb(first);
        int[] secondPixels = pixelRgb(second);

        long sum = 0;
        for (int pixel = 0; pixel < firstPixels.length; pixel++) {
            for (int shift = 16; shift >= 0; shift -= 8) {
                int difference = ((firstPixels[pixel] >>> shift) & 0xFF)
                        - ((secondPixels[pixel] >>> shift) & 0xFF);
                sum += (long) difference * difference;
            }
        }
        return sum;
    }
}
