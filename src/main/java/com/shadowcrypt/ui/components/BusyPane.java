package com.shadowcrypt.ui.components;

import com.shadowcrypt.ui.theme.Theme;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Overlay with a progress bar and message; the wrapped content stays visible. */
public final class BusyPane extends StackPane {

    private final Region overlay = new Region();
    private final ProgressBar progressBar = new ProgressBar();
    private final Label messageLabel = new Label();
    private final VBox box;

    public BusyPane(Node content) {
        getChildren().add(content);

        overlay.getStyleClass().add("sc-busy-overlay");
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        progressBar.setPrefWidth(320);
        messageLabel.getStyleClass().add(Theme.CARD_TITLE);

        box = new VBox(12, progressBar, messageLabel);
        box.getStyleClass().add("sc-busy-box");
        box.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        box.setAlignment(Pos.CENTER_LEFT);

        StackPane.setAlignment(box, Pos.CENTER);
        getChildren().addAll(overlay, box);
        setBusy(false);
    }

    public void showIndeterminate(String message) {
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        messageLabel.setText(message);
        setBusy(true);
    }

    public void showProgress(double progress, String message) {
        progressBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
        messageLabel.setText(message);
        setBusy(true);
    }

    public void hideBusy() {
        setBusy(false);
    }

    private void setBusy(boolean busy) {
        overlay.setVisible(busy);
        overlay.setManaged(busy);
        overlay.setMouseTransparent(!busy);
        box.setVisible(busy);
        box.setManaged(busy);
    }
}
