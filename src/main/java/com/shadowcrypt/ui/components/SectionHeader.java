package com.shadowcrypt.ui.components;

import com.shadowcrypt.ui.theme.Theme;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Consistent section heading with a supporting description. */
public final class SectionHeader extends VBox {

    public SectionHeader(String title, String subtitle) {
        super(4);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add(Theme.TITLE);

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add(Theme.SUBTITLE);
        subtitleLabel.setWrapText(true);

        getChildren().addAll(titleLabel, subtitleLabel);
    }
}
