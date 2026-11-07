package com.shadowcrypt.image.processing;

import com.shadowcrypt.exception.FileSafetyException;
import com.shadowcrypt.exception.SteganographyException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/** Writes normalized RGB images as PNG without silently replacing existing files. */
public final class PngWriter {

    private static final String PNG_FORMAT = "png";

    private PngWriter() {
    }

    public static void write(BufferedImage image, Path file, boolean replaceExisting) {
        Objects.requireNonNull(image, "image");
        Objects.requireNonNull(file, "file");

        Path absolute = file.toAbsolutePath();
        try {
            Path parent = absolute.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (!replaceExisting && Files.exists(absolute)) {
                throw new FileSafetyException("The output image already exists: " + absolute);
            }

            Path temp = Files.createTempFile(
                    parent != null ? parent : Path.of("."), ".shadowcrypt-image", ".png");
            try {
                if (!ImageIO.write(image, PNG_FORMAT, temp.toFile())) {
                    throw new SteganographyException("No PNG image writer is available");
                }
                try {
                    Files.move(temp, absolute,
                            StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(temp);
            }
        } catch (IOException e) {
            throw new SteganographyException("Unable to write the stego image: " + absolute, e);
        }
    }
}
