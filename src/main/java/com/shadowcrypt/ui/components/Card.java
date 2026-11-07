package com.shadowcrypt.ui.components;

import com.shadowcrypt.ui.theme.Theme;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Rounded content container with an optional title. */
public final class Card extends VBox {

    public Card(String title) {
        setSpacing(12);
        getStyleClass().add(Theme.CARD);
        if (title != null && !title.isBlank()) {
            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add(Theme.CARD_TITLE);
            getChildren().add(titleLabel);
        }
    }

    public void add(Node... nodes) {
        getChildren().addAll(nodes);
    }
}
