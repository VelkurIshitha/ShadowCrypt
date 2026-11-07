package com.shadowcrypt.image.normalization;

import com.shadowcrypt.config.AppConstants;
import com.shadowcrypt.exception.SteganographyException;
import com.shadowcrypt.image.models.CarrierImage;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Objects;

/**
 * Loads PNG carriers and normalizes them to opaque {@link BufferedImage#TYPE_INT_RGB}.
 * Any alpha channel is composited over white, so embedding and analysis always see
 * the same deterministic pixel baseline.
 */
public final class CarrierNormalizer {

    private static final String PNG_FORMAT = "png";

    private CarrierNormalizer() {
    }

    public static CarrierImage load(Path file) {
        Objects.requireNonNull(file, "file");
        try (InputStream input = Files.newInputStream(file)) {
            return load(input, file);
        } catch (IOException e) {
            throw new SteganographyException("Unable to open the carrier image: " + file, e);
        }
    }

    public static CarrierImage loadResource(String resourcePath) {
        Objects.requireNonNull(resourcePath, "resourcePath");
        try (InputStream input = CarrierNormalizer.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new SteganographyException("Bundled carrier image not found: " + resourcePath);
            }
            return load(input, null);
        } catch (IOException e) {
            throw new SteganographyException("Unable to read the bundled carrier image: " + resourcePath, e);
        }
    }

    public static CarrierImage load(InputStream input, Path source) {
        Objects.requireNonNull(input, "input");
        try (ImageInputStream imageInput = ImageIO.createImageInputStream(input)) {
            if (imageInput == null) {
                throw new SteganographyException("The image data is not readable");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                throw new SteganographyException("Unsupported image format; a PNG carrier is required");
            }
            ImageReader reader = readers.next();
            try {
                String format = reader.getFormatName();
                if (!PNG_FORMAT.equalsIgnoreCase(format)) {
                    throw new SteganographyException(
                            "Only PNG carrier images are supported (found " + format + ")");
                }
                reader.setInput(imageInput, true, true);
                BufferedImage image = reader.read(0);
                if (image == null) {
                    throw new SteganographyException("The carrier image could not be decoded");
                }
                return new CarrierImage(normalize(image), source);
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw new SteganographyException("Unable to decode the carrier image", e);
        }
    }

    /** Converts any image to opaque RGB, compositing transparency over white. */
    public static BufferedImage normalize(BufferedImage source) {
        Objects.requireNonNull(source, "source");
        int width = source.getWidth();
        int height = source.getHeight();
        if (width <= 0 || height <= 0) {
            throw new SteganographyException("The carrier image has no pixels");
        }
        if (source.getType() == BufferedImage.TYPE_INT_RGB) {
            return copyOf(source);
        }

        BufferedImage normalized = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = normalized.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.drawImage(source, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return normalized;
    }

    private static BufferedImage copyOf(BufferedImage source) {
        BufferedImage copy = new BufferedImage(
                source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        int[] pixels = source.getRGB(0, 0, source.getWidth(), source.getHeight(), null, 0, source.getWidth());
        copy.setRGB(0, 0, source.getWidth(), source.getHeight(), pixels, 0, source.getWidth());
        return copy;
    }
}
