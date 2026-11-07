package com.shadowcrypt.analysis;

import com.shadowcrypt.analysis.heatmap.DifferenceHeatmapGenerator;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HeatmapTest {

    @Test
    void identicalImagesProduceBlackHeatmap() {
        BufferedImage image = singlePixel(0xFF123456);

        BufferedImage heatmap = DifferenceHeatmapGenerator.generate(image, image, 16);

        assertEquals(0xFF000000, heatmap.getRGB(0, 0));
    }

    @Test
    void differencesAreAmplifiedPerChannel() {
        BufferedImage original = singlePixel(0xFF000000);
        BufferedImage stego = singlePixel(0xFF0A0B0C);

        BufferedImage heatmap = DifferenceHeatmapGenerator.generate(original, stego, 16);

        assertEquals(0xFFA0B0C0, heatmap.getRGB(0, 0));
    }

    @Test
    void amplificationIsClampedToChannelMaximum() {
        BufferedImage original = singlePixel(0xFF000000);
        BufferedImage stego = singlePixel(0xFF141414);

        BufferedImage heatmap = DifferenceHeatmapGenerator.generate(original, stego, 16);

        assertEquals(0xFFFFFFFF, heatmap.getRGB(0, 0));
    }

    @Test
    void producesRgbImageWithSameDimensions() {
        BufferedImage original = imageOf(3, 2, 0xFF000000, 0xFF000000, 0xFF000000,
                0xFF000000, 0xFF000000, 0xFF000000);
        BufferedImage stego = imageOf(3, 2, 0xFF010101, 0xFF010101, 0xFF010101,
                0xFF010101, 0xFF010101, 0xFF010101);

        BufferedImage heatmap = DifferenceHeatmapGenerator.generate(original, stego, 16);

        assertEquals(3, heatmap.getWidth());
        assertEquals(2, heatmap.getHeight());
        assertEquals(BufferedImage.TYPE_INT_RGB, heatmap.getType());
    }

    @Test
    void rejectsInvalidAmplificationAndMismatchedSizes() {
        BufferedImage image = singlePixel(0xFF000000);

        assertThrows(IllegalArgumentException.class,
                () -> DifferenceHeatmapGenerator.generate(image, image, 0));
        assertThrows(IllegalArgumentException.class, () -> DifferenceHeatmapGenerator.generate(
                image, new BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB), 16));
    }

    private static BufferedImage singlePixel(int argb) {
        return imageOf(1, 1, argb);
    }

    private static BufferedImage imageOf(int width, int height, int... argbPixels) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, width, height, argbPixels, 0, width);
        return image;
    }
}
