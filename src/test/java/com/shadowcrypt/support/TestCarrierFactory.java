package com.shadowcrypt.support;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Random;

/** Builds deterministic PNG carriers for tests without committing binary fixtures. */
public final class TestCarrierFactory {

    private TestCarrierFactory() {
    }

    public static BufferedImage randomCarrier(int width, int height, long seed) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Random random = new Random(seed);
        int[] pixels = new int[width * height];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = random.nextInt() | 0xFF000000;
        }
        image.setRGB(0, 0, width, height, pixels, 0, width);
        return image;
    }

    public static byte[] pngBytes(BufferedImage image) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (!ImageIO.write(image, "png", out)) {
                throw new IllegalStateException("No PNG writer available");
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Path writePng(BufferedImage image, Path directory, String name) throws IOException {
        Path file = directory.resolve(name);
        if (!ImageIO.write(image, "png", file.toFile())) {
            throw new IllegalStateException("No PNG writer available");
        }
        return file;
    }

    /**
     * Builds a one-pixel-wide carrier whose theoretical capacity is exactly the
     * requested byte count, for capacity-boundary tests.
     */
    public static BufferedImage carrierWithCapacityExactly(int capacityBytes) {
        if (capacityBytes < 0) {
            throw new IllegalArgumentException("Capacity must not be negative");
        }
        for (int height = 1; height <= 10_000_000; height++) {
            long capacity = (3L * height) / 8;
            if (capacity == capacityBytes) {
                return randomCarrier(1, height, height);
            }
            if (capacity > capacityBytes) {
                break;
            }
        }
        throw new IllegalArgumentException("No carrier has exactly " + capacityBytes + " bytes of capacity");
    }
}
