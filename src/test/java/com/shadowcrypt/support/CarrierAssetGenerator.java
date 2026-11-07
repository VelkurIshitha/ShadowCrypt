package com.shadowcrypt.support;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/**
 * One-off development utility that generates the bundled PNG carriers. It is kept
 * in test sources so no generator ships with the application; the produced PNGs are
 * committed under {@code src/main/resources/images/carriers}.
 *
 * <p>Run with: {@code java -cp target/test-classes;target/classes
 * com.shadowcrypt.support.CarrierAssetGenerator <output-directory>}</p>
 */
public final class CarrierAssetGenerator {

    private CarrierAssetGenerator() {
    }

    public static void main(String[] args) throws IOException {
        Path outputDirectory = Path.of(args.length > 0 ? args[0] : "src/main/resources/images/carriers");
        Files.createDirectories(outputDirectory);

        write(outputDirectory.resolve("carrier-blue.png"), blueGradient(1200, 800));
        write(outputDirectory.resolve("carrier-texture.png"), texture(800, 600));
        write(outputDirectory.resolve("carrier-warm.png"), warmGradient(512, 512));

        System.out.println("Wrote bundled carriers to " + outputDirectory.toAbsolutePath());
    }

    private static BufferedImage blueGradient(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Random random = new Random(101L);
        for (int y = 0; y < height; y++) {
            double vertical = (double) y / Math.max(1, height - 1);
            for (int x = 0; x < width; x++) {
                double horizontal = (double) x / Math.max(1, width - 1);
                int red = clamp(18 + (int) (40 * horizontal) + random.nextInt(5));
                int green = clamp(48 + (int) (80 * vertical) + random.nextInt(5));
                int blue = clamp(120 + (int) (100 * vertical) - (int) (30 * horizontal)
                        + random.nextInt(5));
                image.setRGB(x, y, (red << 16) | (green << 8) | blue);
            }
        }
        return image;
    }

    private static BufferedImage texture(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Random random = new Random(202L);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double wave = Math.sin(x / 23.0) + Math.cos(y / 31.0);
                int base = 96 + (int) (28 * wave);
                int grain = random.nextInt(26);
                int red = clamp(base + grain);
                int green = clamp(base + 12 + grain / 2);
                int blue = clamp(base - 10 + grain);
                image.setRGB(x, y, (red << 16) | (green << 8) | blue);
            }
        }
        return image;
    }

    private static BufferedImage warmGradient(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Random random = new Random(303L);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double diagonal = (double) (x + y) / Math.max(1, width + height - 2);
                int red = clamp(150 + (int) (95 * diagonal) + random.nextInt(6));
                int green = clamp(80 + (int) (90 * (1.0 - diagonal)) + random.nextInt(6));
                int blue = clamp(40 + (int) (60 * diagonal) + random.nextInt(6));
                image.setRGB(x, y, (red << 16) | (green << 8) | blue);
            }
        }
        return image;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static void write(Path file, BufferedImage image) throws IOException {
        if (!ImageIO.write(image, "png", file.toFile())) {
            throw new IOException("No PNG writer available");
        }
    }
}
