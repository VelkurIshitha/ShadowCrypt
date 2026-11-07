package com.shadowcrypt.model;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Real image-impact measurements produced from one encryption run. Every value is
 * computed from the actual normalized carrier, stego image, and written files.
 */
public record AnalysisReport(
        int width,
        int height,
        long originalPngBytes,
        long stegoPngBytes,
        long payloadBytes,
        long packageBytes,
        long capacityBytes,
        double mse,
        double psnr,
        long embeddingNanos,
        BufferedImage normalizedCarrier,
        BufferedImage stegoImage,
        BufferedImage differenceHeatmap) {

    public AnalysisReport {
        Objects.requireNonNull(normalizedCarrier, "normalizedCarrier");
        Objects.requireNonNull(stegoImage, "stegoImage");
        Objects.requireNonNull(differenceHeatmap, "differenceHeatmap");
    }

    public long absoluteFileSizeDifference() {
        return stegoPngBytes - originalPngBytes;
    }

    public double percentageFileSizeDifference() {
        return originalPngBytes == 0 ? 0.0 : (stegoPngBytes - originalPngBytes) * 100.0 / originalPngBytes;
    }

    public double capacityUtilizationPercent() {
        return capacityBytes == 0 ? 0.0 : packageBytes * 100.0 / capacityBytes;
    }
}
