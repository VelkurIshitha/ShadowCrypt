package com.shadowcrypt.ui.decryption;

import com.shadowcrypt.ui.components.BusyPane;
import com.shadowcrypt.ui.components.Card;
import com.shadowcrypt.ui.components.FilePickerField;
import com.shadowcrypt.ui.components.ImagePreviewPane;
import com.shadowcrypt.ui.components.MetricGrid;
import com.shadowcrypt.ui.components.SectionHeader;
import com.shadowcrypt.ui.components.StatusBanner;
import com.shadowcrypt.ui.theme.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Decryption screen: stego image, private key, passphrase, and recovered payload. */
public final class DecryptionView extends StackPane {

    private final FilePickerField stegoPicker = new FilePickerField("Choose the stego PNG image");
    private final ImagePreviewPane stegoPreview = new ImagePreviewPane("Stego image");

    private final FilePickerField privateKeyPicker =
            new FilePickerField("Choose your protected private-key file");
    private final PasswordField passphraseField = new PasswordField();

    private final FilePickerField outputDirectoryPicker =
            new FilePickerField("Choose the folder for recovered files");

    private final Button decryptButton = new Button("Decrypt & Extract");
    private final StatusBanner statusBanner = new StatusBanner();

    private final Card resultCard = new Card(null);
    private final Label resultTitle = new Label("Recovered payload");
    private final TextArea recoveredText = new TextArea();
    private final MetricGrid resultMetrics = new MetricGrid();
    private final Label shaLabel = new Label("SHA-256  —");
    private final Label authenticationLabel = new Label("AES-256-GCM authentication  —");

    private final BusyPane busyPane;

    public DecryptionView() {
        passphraseField.setPromptText("Passphrase for the private-key file");

        Label stegoHint = new Label(
                "Only PNG images produced by ShadowCrypt contain a readable package.");
        stegoHint.getStyleClass().add(Theme.MUTED);
        stegoHint.setWrapText(true);

        VBox stegoInfo = new VBox(8, stegoHint);
        HBox.setHgrow(stegoInfo, Priority.ALWAYS);

        HBox stegoRow = new HBox(20, stegoPreview, stegoInfo);
        stegoRow.setAlignment(Pos.TOP_LEFT);

        Label passphraseLabel = new Label("Passphrase");
        passphraseLabel.getStyleClass().add(Theme.MUTED);

        Card stegoCard = new Card("Stego image");
        stegoCard.add(stegoPicker, stegoRow);

        Card keyCard = new Card("Private key");
        keyCard.add(privateKeyPicker, passphraseLabel, passphraseField);

        Label outputHint = new Label(
                "Recovered files are written only inside this folder; existing files are never replaced silently.");
        outputHint.getStyleClass().add(Theme.MUTED);
        outputHint.setWrapText(true);

        Card outputCard = new Card("Output location");
        outputCard.add(outputDirectoryPicker, outputHint);

        decryptButton.getStyleClass().add(Theme.PRIMARY_BUTTON);

        resultTitle.getStyleClass().add(Theme.CARD_TITLE);
        recoveredText.setEditable(false);
        recoveredText.setWrapText(true);
        recoveredText.setPrefRowCount(8);
        shaLabel.getStyleClass().add(Theme.CODE);
        shaLabel.setWrapText(true);
        authenticationLabel.getStyleClass().add(Theme.MUTED);
        authenticationLabel.setWrapText(true);

        resultCard.add(resultTitle, recoveredText, resultMetrics, shaLabel, authenticationLabel);
        resultCard.setVisible(false);
        resultCard.setManaged(false);

        VBox content = new VBox(16);
        content.setPadding(new Insets(24, 24, 32, 24));

        VBox actions = new VBox(12, new HBox(14, decryptButton), statusBanner);
        content.getChildren().addAll(
                new SectionHeader("Decryption",
                        "Extract the package from a stego PNG, unwrap the AES key with your "
                                + "passphrase-protected private key, and recover the original payload."),
                stegoCard,
                keyCard,
                outputCard,
                actions,
                resultCard);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        busyPane = new BusyPane(scroll);
        getChildren().add(busyPane);
        setRecoveredTextVisible(false);
    }

    public FilePickerField stegoPicker() {
        return stegoPicker;
    }

    public ImagePreviewPane stegoPreview() {
        return stegoPreview;
    }

    public FilePickerField privateKeyPicker() {
        return privateKeyPicker;
    }

    public PasswordField passphraseField() {
        return passphraseField;
    }

    public String passphrase() {
        String text = passphraseField.getText();
        return text == null ? "" : text;
    }

    public void clearPassphrase() {
        passphraseField.clear();
    }

    public FilePickerField outputDirectoryPicker() {
        return outputDirectoryPicker;
    }

    public Button decryptButton() {
        return decryptButton;
    }

    public StatusBanner statusBanner() {
        return statusBanner;
    }

    public BusyPane busyPane() {
        return busyPane;
    }

    public Card resultCard() {
        return resultCard;
    }

    public MetricGrid resultMetrics() {
        return resultMetrics;
    }

    public void showResultCard(String title) {
        resultTitle.setText(title);
        resultMetrics.clearMetrics();
        resultCard.setVisible(true);
        resultCard.setManaged(true);
    }

    public void setRecoveredTextVisible(boolean visible) {
        recoveredText.setVisible(visible);
        recoveredText.setManaged(visible);
    }

    public TextArea recoveredText() {
        return recoveredText;
    }

    public void setSha(String sha256) {
        shaLabel.setText("SHA-256  " + sha256);
    }

    public void setAuthentication(String message, boolean success) {
        authenticationLabel.setText(message);
        authenticationLabel.getStyleClass().setAll(success ? Theme.SUCCESS_TEXT : Theme.ERROR_TEXT);
    }
}
