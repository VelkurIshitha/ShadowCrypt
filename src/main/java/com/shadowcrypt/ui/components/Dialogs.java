package com.shadowcrypt.ui.components;

import com.shadowcrypt.ui.theme.Theme;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.util.Optional;

/** Thin wrappers around JavaFX alerts with the application theme applied. */
public final class Dialogs {

    private Dialogs() {
    }

    public static boolean confirm(Window owner, String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle(title);
        alert.setHeaderText(header);
        prepare(alert, owner);
        Optional<ButtonType> choice = alert.showAndWait();
        return choice.isPresent() && choice.get() == ButtonType.OK;
    }

    public static void error(Window owner, String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(header);
        prepare(alert, owner);
        alert.showAndWait();
    }

    public static void info(Window owner, String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(header);
        prepare(alert, owner);
        alert.showAndWait();
    }

    private static void prepare(Alert alert, Window owner) {
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.getDialogPane().setMinWidth(440);
        Theme.applyTo(alert.getDialogPane());
    }
}
