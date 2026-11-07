package com.shadowcrypt.image;

import com.shadowcrypt.exception.FileSafetyException;
import com.shadowcrypt.image.normalization.CarrierNormalizer;
import com.shadowcrypt.image.processing.PngWriter;
import com.shadowcrypt.support.TestCarrierFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PngRoundTripTest {

    @TempDir
    Path tempDirectory;

    @Test
    void writesAndReloadsIdenticalPixels() {
        BufferedImage original = TestCarrierFactory.randomCarrier(40, 30, 12L);
        Path file = tempDirectory.resolve("nested").resolve("image.png");

        PngWriter.write(original, file, false);
        BufferedImage reloaded = CarrierNormalizer.load(file).image();

        assertTrue(java.nio.file.Files.exists(file));
        assertArrayEquals(pixels(original), pixels(reloaded));
    }

    @Test
    void refusesToOverwriteUnlessAllowed() {
        BufferedImage image = TestCarrierFactory.randomCarrier(8, 8, 13L);
        Path file = tempDirectory.resolve("guarded.png");

        PngWriter.write(image, file, false);

        assertThrows(FileSafetyException.class, () -> PngWriter.write(image, file, false));
        PngWriter.write(image, file, true);
    }

    private static int[] pixels(BufferedImage image) {
        return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
    }
}
