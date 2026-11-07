package com.shadowcrypt.ui.components;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.util.Objects;

/** Converts AWT images for JavaFX display without leaking AWT types into views. */
public final class ImageConverter {

    private ImageConverter() {
    }

    public static Image toFxImage(BufferedImage image) {
        Objects.requireNonNull(image, "image");
        return SwingFXUtils.toFXImage(image, null);
    }
}
