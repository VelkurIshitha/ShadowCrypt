package com.shadowcrypt.ui.components;

import com.shadowcrypt.ui.theme.Theme;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/** Inline success / warning / error / info message that can be hidden. */
public final class StatusBanner extends HBox {

    private final Label messageLabel = new Label();

    public StatusBanner() {
        setSpacing(8);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add(Theme.BANNER);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(messageLabel, javafx.scene.layout.Priority.ALWAYS);
        getChildren().add(messageLabel);
        hide();
    }

    public void success(String message) {
        show(message, Theme.BANNER_SUCCESS);
    }

    public void warning(String message) {
        show(message, Theme.BANNER_WARNING);
    }

    public void error(String message) {
        show(message, Theme.BANNER_ERROR);
    }

    public void info(String message) {
        show(message, Theme.BANNER_INFO);
    }

    public void hide() {
        messageLabel.setText("");
        setVisible(false);
        setManaged(false);
    }

    private void show(String message, String variant) {
        messageLabel.setText(message);
        getStyleClass().setAll(Theme.BANNER, variant);
        setVisible(true);
        setManaged(true);
    }
}
