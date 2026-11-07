package com.shadowcrypt.ui.analysis;

import com.shadowcrypt.ui.components.Card;
import com.shadowcrypt.ui.components.ImagePreviewPane;
import com.shadowcrypt.ui.components.MetricGrid;
import com.shadowcrypt.ui.components.SectionHeader;
import com.shadowcrypt.ui.theme.Theme;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.awt.image.BufferedImage;

/** Image-impact analysis of the most recent real encryption run. */
public final class AnalysisView extends ScrollPane {

    private final Label emptyState = new Label(
            "No analysis yet. Run an encryption to generate real image-impact results.");
    private final Label contextLabel = new Label();
    private final Card contentCard = new Card(null);

    private final ImagePreviewPane carrierPreview = new ImagePreviewPane("Normalized carrier");
    private final ImagePreviewPane stegoPreview = new ImagePreviewPane("Stego image");
    private final ImagePreviewPane heatmapPreview = new ImagePreviewPane("Difference heatmap (×16)");
    private final MetricGrid metricGrid = new MetricGrid();

    public AnalysisView() {
        emptyState.getStyleClass().add(Theme.MUTED);
        emptyState.setWrapText(true);
        emptyState.setPadding(new Insets(4, 0, 0, 0));

        contextLabel.getStyleClass().add(Theme.MUTED);
        contextLabel.setWrapText(true);

        FlowPane previews = new FlowPane(18, 18,
                carrierPreview, stegoPreview, heatmapPreview);

        Card metricsCard = new Card("Metrics");
        metricsCard.add(metricGrid);

        contentCard.add(contextLabel, previews, metricsCard);

        VBox content = new VBox(16);
        content.setPadding(new Insets(24, 24, 32, 24));
        content.getChildren().addAll(
                new SectionHeader("Image Analysis",
                        "Compare the normalized carrier with the produced stego image using "
                                + "measurements from the actual encryption operation."),
                emptyState,
                contentCard);

        setContent(content);
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setHasAnalysis(false);
    }

    public void setHasAnalysis(boolean hasAnalysis) {
        emptyState.setVisible(!hasAnalysis);
        emptyState.setManaged(!hasAnalysis);
        contentCard.setVisible(hasAnalysis);
        contentCard.setManaged(hasAnalysis);
    }

    public void setContext(String text) {
        contextLabel.setText(text);
    }

    public void setPreviews(BufferedImage carrier, BufferedImage stego, BufferedImage heatmap,
                            int width, int height) {
        String dimensions = width + "×" + height;
        carrierPreview.setImage(carrier, "Normalized carrier • " + dimensions);
        stegoPreview.setImage(stego, "Stego image • " + dimensions);
        heatmapPreview.setImage(heatmap, "Amplified absolute difference • " + dimensions);
    }

    public MetricGrid metricGrid() {
        return metricGrid;
    }
}
