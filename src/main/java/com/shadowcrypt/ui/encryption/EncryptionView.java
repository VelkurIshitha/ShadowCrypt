package com.shadowcrypt.ui.encryption;

import com.shadowcrypt.config.AppConstants;
import com.shadowcrypt.ui.components.BusyPane;
import com.shadowcrypt.ui.components.Card;
import com.shadowcrypt.ui.components.FilePickerField;
import com.shadowcrypt.ui.components.ImagePreviewPane;
import com.shadowcrypt.ui.components.SectionHeader;
import com.shadowcrypt.ui.components.StatusBanner;
import com.shadowcrypt.ui.theme.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

/** Encryption screen: payload, carrier, recipient key, and output. */
public final class EncryptionView extends StackPane {

    private final ToggleButton textModeButton = new ToggleButton("Text");
    private final ToggleButton fileModeButton = new ToggleButton("File");

    private final TextArea textInput = new TextArea();
    private final FilePickerField sourcePicker = new FilePickerField("Choose any file to encrypt");
    private final Label fileInfoLabel = new Label(
            "Any file type is supported — it is treated as raw binary data.");
    private final VBox fileBox = new VBox(8, sourcePicker, fileInfoLabel);

    private final FilePickerField carrierPicker = new FilePickerField("Choose a PNG carrier image");
    private final ImagePreviewPane carrierPreview = new ImagePreviewPane("Carrier & result preview");
    private final Label capacityLabel = new Label("Select a PNG carrier image to see its capacity.");

    private final Button demoButton = new Button("Load Demo Preset");
    private final List<Button> galleryButtons = List.of(
            new Button(AppConstants.BUNDLED_CARRIER_LABELS[0]),
            new Button(AppConstants.BUNDLED_CARRIER_LABELS[1]),
            new Button(AppConstants.BUNDLED_CARRIER_LABELS[2]));

    private final Button importKeyButton = new Button("Import public key…");
    private final Label fingerprintLabel = new Label("No recipient public key selected");
    private final Label keyOriginLabel = new Label(
            "Generate or import a key in Key Management, or import a .scpub file here.");

    private final FilePickerField outputPicker = new FilePickerField("Choose where to save the stego PNG");
    private final Button encryptButton = new Button("Encrypt & Embed");
    private final StatusBanner statusBanner = new StatusBanner();
    private final BusyPane busyPane;

    public EncryptionView() {
        ToggleGroup modeGroup = new ToggleGroup();
        textModeButton.setToggleGroup(modeGroup);
        fileModeButton.setToggleGroup(modeGroup);
        textModeButton.setSelected(true);
        modeGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (newToggle == null && oldToggle != null) {
                modeGroup.selectToggle(oldToggle);
                return;
            }
            updateModeVisibility();
        });

        textInput.setPromptText("Type or paste the text to encrypt…");
        textInput.setWrapText(true);
        textInput.setPrefRowCount(6);

        fileInfoLabel.getStyleClass().add(Theme.MUTED);
        fileInfoLabel.setWrapText(true);

        capacityLabel.getStyleClass().add(Theme.MUTED);
        capacityLabel.setWrapText(true);

        fingerprintLabel.getStyleClass().add(Theme.CODE);
        fingerprintLabel.setWrapText(true);
        keyOriginLabel.getStyleClass().add(Theme.MUTED);
        keyOriginLabel.setWrapText(true);

        encryptButton.getStyleClass().add(Theme.PRIMARY_BUTTON);
        encryptButton.setDefaultButton(true);

        VBox content = new VBox(16);
        content.setPadding(new Insets(24, 24, 32, 24));

        content.getChildren().addAll(
                new SectionHeader("Encryption",
                        "Encrypt text or any file with AES-256-GCM, wrap the session key for the "
                                + "recipient, and hide the package in a PNG's least significant bits."),
                buildPayloadCard(),
                buildCarrierCard(),
                buildRecipientCard(),
                buildOutputCard(),
                new VBox(12, new HBox(14, encryptButton), statusBanner));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        busyPane = new BusyPane(scroll);
        getChildren().add(busyPane);
        updateModeVisibility();
    }

    private VBox buildPayloadCard() {
        Card card = new Card("Payload");
        Label modeLabel = new Label("Payload type");
        modeLabel.getStyleClass().add(Theme.MUTED);
        HBox modeRow = new HBox(8, modeLabel, textModeButton, fileModeButton);
        modeRow.setAlignment(Pos.CENTER_LEFT);

        card.add(modeRow, textInput, fileBox);
        return card;
    }

    private VBox buildCarrierCard() {
        Card card = new Card("Carrier image (PNG only)");

        HBox gallery = new HBox(8);
        gallery.setAlignment(Pos.CENTER_LEFT);
        gallery.getChildren().addAll(galleryButtons);
        gallery.getChildren().add(demoButton);

        VBox info = new VBox(10, capacityLabel);
        info.setFillWidth(true);
        HBox.setHgrow(info, Priority.ALWAYS);

        HBox previewRow = new HBox(20, carrierPreview, info);
        previewRow.setAlignment(Pos.TOP_LEFT);

        card.add(carrierPicker, gallery, previewRow);
        return card;
    }

    private VBox buildRecipientCard() {
        Card card = new Card("Recipient public key");
        card.add(importKeyButton, fingerprintLabel, keyOriginLabel);
        return card;
    }

    private VBox buildOutputCard() {
        Card card = new Card("Output");
        card.add(outputPicker);
        return card;
    }

    private void updateModeVisibility() {
        boolean textMode = textModeButton.isSelected();
        textInput.setVisible(textMode);
        textInput.setManaged(textMode);
        fileBox.setVisible(!textMode);
        fileBox.setManaged(!textMode);
    }

    public boolean isTextMode() {
        return textModeButton.isSelected();
    }

    public void selectTextMode() {
        textModeButton.setSelected(true);
        updateModeVisibility();
    }

    public void setOnModeChanged(Runnable action) {
        textModeButton.selectedProperty().addListener(
                (observable, oldValue, newValue) -> action.run());
    }

    public void setText(String text) {
        textInput.setText(text == null ? "" : text);
    }

    public TextArea textInput() {
        return textInput;
    }

    public FilePickerField sourcePicker() {
        return sourcePicker;
    }

    public Label fileInfoLabel() {
        return fileInfoLabel;
    }

    public FilePickerField carrierPicker() {
        return carrierPicker;
    }

    public ImagePreviewPane carrierPreview() {
        return carrierPreview;
    }

    public Label capacityLabel() {
        return capacityLabel;
    }

    public Button demoButton() {
        return demoButton;
    }

    public List<Button> galleryButtons() {
        return galleryButtons;
    }

    public Button importKeyButton() {
        return importKeyButton;
    }

    public Label fingerprintLabel() {
        return fingerprintLabel;
    }

    public Label keyOriginLabel() {
        return keyOriginLabel;
    }

    public FilePickerField outputPicker() {
        return outputPicker;
    }

    public Button encryptButton() {
        return encryptButton;
    }

    public StatusBanner statusBanner() {
        return statusBanner;
    }

    public BusyPane busyPane() {
        return busyPane;
    }
}
