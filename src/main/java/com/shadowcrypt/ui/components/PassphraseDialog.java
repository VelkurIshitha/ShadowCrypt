package com.shadowcrypt.ui.components;

import com.shadowcrypt.ui.theme.Theme;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * Masked passphrase prompt. In confirmation mode the two entries must match before
 * the dialog closes. Returns null when the user cancels.
 */
public final class PassphraseDialog {

    private PassphraseDialog() {
    }

    public static char[] request(Window owner, String title, boolean requireConfirmation) {
        Dialog<char[]> dialog = new Dialog<>();
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.setTitle(title);
        dialog.setHeaderText(requireConfirmation
                ? "Choose a passphrase that protects the private key on disk."
                : "Enter the passphrase for the private-key file.");

        ButtonType continueButton = new ButtonType("Continue", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(continueButton, ButtonType.CANCEL);

        PasswordField first = new PasswordField();
        first.setPromptText("Passphrase");
        PasswordField second = new PasswordField();
        second.setPromptText("Confirm passphrase");

        Label hint = new Label("The passphrase is never stored. It derives the AES key "
                + "used to protect the private key (PBKDF2-HMAC-SHA256).");
        hint.getStyleClass().add(Theme.MUTED);
        hint.setWrapText(true);

        Label error = new Label();
        error.getStyleClass().add(Theme.ERROR_TEXT);
        error.setVisible(false);
        error.setManaged(false);

        VBox content = new VBox(10, first);
        if (requireConfirmation) {
            content.getChildren().add(second);
        }
        content.getChildren().addAll(hint, error);
        content.setPrefWidth(430);
        dialog.getDialogPane().setContent(content);
        Theme.applyTo(dialog.getDialogPane());
        dialog.setResultConverter(button -> button == continueButton ? first.getText().toCharArray() : null);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(continueButton);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            String value = first.getText();
            if (value == null || value.isEmpty()) {
                showError(error, "The passphrase must not be empty.");
                event.consume();
                return;
            }
            if (requireConfirmation && !value.equals(second.getText())) {
                showError(error, "The two passphrases do not match.");
                event.consume();
            }
        });

        return dialog.showAndWait().orElse(null);
    }

    private static void showError(Label error, String message) {
        error.setText(message);
        error.setVisible(true);
        error.setManaged(true);
    }
}
