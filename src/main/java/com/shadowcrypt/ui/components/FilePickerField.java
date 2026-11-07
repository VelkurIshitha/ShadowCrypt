package com.shadowcrypt.ui.components;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.nio.file.Path;

/** Read-only path field plus a Browse button. */
public final class FilePickerField extends HBox {

    private final TextField field = new TextField();
    private final Button browseButton = new Button("Browse…");

    public FilePickerField(String prompt) {
        super(8);
        field.setPromptText(prompt);
        field.setEditable(false);
        field.setFocusTraversable(false);
        HBox.setHgrow(field, Priority.ALWAYS);
        getChildren().addAll(field, browseButton);
    }

    public void setOnBrowse(Runnable action) {
        browseButton.setOnAction(event -> action.run());
    }

    public void setPath(Path path) {
        field.setText(path == null ? "" : path.toString());
    }

    public Path getPath() {
        String text = field.getText();
        if (text == null || text.isBlank()) {
            return null;
        }
        return Path.of(text.trim());
    }

    public void clear() {
        field.clear();
    }

    public void setBrowseDisabled(boolean disabled) {
        browseButton.setDisable(disabled);
    }
}
