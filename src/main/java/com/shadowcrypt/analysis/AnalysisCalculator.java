package com.shadowcrypt.analysis;

import com.shadowcrypt.analysis.heatmap.DifferenceHeatmapGenerator;
import com.shadowcrypt.analysis.metrics.MseCalculator;
import com.shadowcrypt.analysis.metrics.PsnrCalculator;
import com.shadowcrypt.config.AppConstants;
import com.shadowcrypt.model.AnalysisReport;

import java.awt.image.BufferedImage;
import java.util.Objects;

/** Computes the complete image-impact report from real encryption outputs. */
public final class AnalysisCalculator {

    private AnalysisCalculator() {
    }

    public static AnalysisReport calculate(
            BufferedImage normalizedCarrier,
            BufferedImage stegoImage,
            long originalPngBytes,
            long stegoPngBytes,
            long payloadBytes,
            long packageBytes,
            long capacityBytes,
            long embeddingNanos) {

        Objects.requireNonNull(normalizedCarrier, "normalizedCarrier");
        Objects.requireNonNull(stegoImage, "stegoImage");

        double mse = MseCalculator.calculate(normalizedCarrier, stegoImage);
        double psnr = PsnrCalculator.calculate(mse);
        BufferedImage differenceHeatmap = DifferenceHeatmapGenerator.generate(
                normalizedCarrier, stegoImage, AppConstants.HEATMAP_AMPLIFICATION);

        return new AnalysisReport(
                normalizedCarrier.getWidth(),
                normalizedCarrier.getHeight(),
                originalPngBytes,
                stegoPngBytes,
                payloadBytes,
                packageBytes,
                capacityBytes,
                mse,
                psnr,
                embeddingNanos,
                normalizedCarrier,
                stegoImage,
                differenceHeatmap);
    }
}
