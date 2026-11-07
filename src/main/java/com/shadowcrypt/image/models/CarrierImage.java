package com.shadowcrypt.image.models;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Objects;

/**
 * A carrier image that has already been normalized to opaque RGB. The baseline
 * stored here is what the analysis screen compares the stego image against.
 */
public record CarrierImage(BufferedImage image, Path source) {

    public CarrierImage {
        Objects.requireNonNull(image, "image");
    }

    public int width() {
        return image.getWidth();
    }

    public int height() {
        return image.getHeight();
    }
}
