package com.shadowcrypt.image;

import com.shadowcrypt.exception.SteganographyException;
import com.shadowcrypt.image.models.CarrierImage;
import com.shadowcrypt.image.normalization.CarrierNormalizer;
import com.shadowcrypt.support.TestCarrierFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CarrierNormalizerTest {

    @TempDir
    Path tempDirectory;

    @Test
    void loadsPngAndPreservesOpaquePixels() {
        BufferedImage original = TestCarrierFactory.randomCarrier(48, 32, 1L);

        CarrierImage loaded = CarrierNormalizer.load(
                new ByteArrayInputStream(TestCarrierFactory.pngBytes(original)), null);

        assertEquals(48, loaded.width());
        assertEquals(32, loaded.height());
        assertEquals(BufferedImage.TYPE_INT_RGB, loaded.image().getType());
        assertArrayEquals(pixels(original), pixels(loaded.image()));
    }

    @Test
    void rejectsJpegData() throws IOException {
        BufferedImage image = TestCarrierFactory.randomCarrier(32, 32, 2L);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", out);

        assertThrows(SteganographyException.class, () -> CarrierNormalizer.load(
                new ByteArrayInputStream(out.toByteArray()), null));
    }

    @Test
    void compositesTransparencyOverWhite() {
        BufferedImage transparent = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                transparent.setRGB(x, y, new Color(0, 0, 0, 0).getRGB());
            }
        }

        BufferedImage normalized = CarrierNormalizer.normalize(transparent);

        assertEquals(BufferedImage.TYPE_INT_RGB, normalized.getType());
        for (int pixel : pixels(normalized)) {
            assertEquals(0xFFFFFF, pixel & 0xFFFFFF);
        }
    }

    @Test
    void rejectsMissingFile() {
        Path missing = tempDirectory.resolve("missing.png");

        assertThrows(SteganographyException.class, () -> CarrierNormalizer.load(missing));
    }

    @Test
    void normalizeReturnsIndependentCopy() {
        BufferedImage original = TestCarrierFactory.randomCarrier(8, 8, 3L);
        BufferedImage normalized = CarrierNormalizer.normalize(original);
        int normalizedPixel = normalized.getRGB(0, 0);

        original.setRGB(0, 0, 0x123456);

        assertEquals(normalizedPixel, normalized.getRGB(0, 0));
    }

    private static int[] pixels(BufferedImage image) {
        return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
    }
}
