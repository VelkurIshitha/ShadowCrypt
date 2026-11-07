package com.shadowcrypt.ui.analysis;

import com.shadowcrypt.model.AnalysisReport;
import com.shadowcrypt.model.EncryptionResult;
import com.shadowcrypt.ui.components.Formatters;
import com.shadowcrypt.ui.main.AppState;

import java.util.Locale;

/** Renders the real analysis report produced by the most recent encryption. */
public final class AnalysisController {

    private final AnalysisView view;
    private final AppState state;

    public AnalysisController(AnalysisView view, AppState state) {
        this.view = view;
        this.state = state;

        state.lastAnalysisProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                render(newValue);
            }
        });
        if (state.lastAnalysisProperty().get() != null) {
            render(state.lastAnalysisProperty().get());
        }
    }

    private void render(AnalysisReport report) {
        EncryptionResult encryptionResult = state.lastEncryptionProperty().get();
        view.setHasAnalysis(true);
        view.setContext(encryptionResult == null
                ? "Measured from the most recent encryption."
                : "Stego image: " + encryptionResult.stegoFile());
        view.setPreviews(report.normalizedCarrier(), report.stegoImage(),
                report.differenceHeatmap(), report.width(), report.height());

        view.metricGrid().clearMetrics();
        view.metricGrid().addMetric("Dimensions", report.width() + " × " + report.height());
        view.metricGrid().addMetric("Original PNG size", Formatters.formatBytes(report.originalPngBytes()));
        view.metricGrid().addMetric("Stego PNG size", Formatters.formatBytes(report.stegoPngBytes()));
        view.metricGrid().addMetric("Absolute size difference",
                Formatters.formatBytes(report.absoluteFileSizeDifference()));
        view.metricGrid().addMetric("Percentage size difference",
                Formatters.formatPercent(report.percentageFileSizeDifference()));
        view.metricGrid().addMetric("Original payload size", Formatters.formatBytes(report.payloadBytes()));
        view.metricGrid().addMetric("Complete package size", Formatters.formatBytes(report.packageBytes()));
        view.metricGrid().addMetric("Theoretical capacity", Formatters.formatBytes(report.capacityBytes()));
        view.metricGrid().addMetric("Capacity utilization",
                Formatters.formatPercent(report.capacityUtilizationPercent()));
        view.metricGrid().addMetric("MSE", String.format(Locale.ROOT, "%.6f", report.mse()));
        view.metricGrid().addMetric("PSNR", Double.isInfinite(report.psnr())
                ? "∞ (identical images)"
                : String.format(Locale.ROOT, "%.2f dB", report.psnr()));
        view.metricGrid().addMetric("Embedding time",
                Formatters.formatDurationMillis(report.embeddingNanos()));
    }
}
