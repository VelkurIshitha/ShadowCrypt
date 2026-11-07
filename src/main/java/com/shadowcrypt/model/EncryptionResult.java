package com.shadowcrypt.model;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Outcome of a real encryption run. Images and sizes are captured from the actual
 * operation so the analysis screen never has to invent or recompute them.
 */
public record EncryptionResult(
        Path stegoFile,
        PayloadType payloadType,
        long payloadBytes,
        long packageBytes,
        long capacityBytes,
        int width,
        int height,
        long originalPngBytes,
        long stegoPngBytes,
        long embeddingNanos,
        BufferedImage normalizedCarrier,
        BufferedImage stegoImage) {

    public EncryptionResult {
        Objects.requireNonNull(stegoFile, "stegoFile");
        Objects.requireNonNull(payloadType, "payloadType");
        Objects.requireNonNull(normalizedCarrier, "normalizedCarrier");
        Objects.requireNonNull(stegoImage, "stegoImage");
    }

    public double capacityUtilizationPercent() {
        return capacityBytes == 0 ? 0.0 : packageBytes * 100.0 / capacityBytes;
    }
}
