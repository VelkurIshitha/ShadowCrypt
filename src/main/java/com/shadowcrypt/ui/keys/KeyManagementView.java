package com.shadowcrypt.ui.keys;

import com.shadowcrypt.ui.components.BusyPane;
import com.shadowcrypt.ui.components.Card;
import com.shadowcrypt.ui.components.SectionHeader;
import com.shadowcrypt.ui.components.StatusBanner;
import com.shadowcrypt.ui.theme.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Key management screen: generation, fingerprints, public exchange, protected storage. */
public final class KeyManagementView extends StackPane {

    private final Button generateButton = new Button("Generate RSA-3072 key pair");
    private final Label sessionStateLabel = new Label("No key pair in this session.");
    private final Label fingerprintLabel = new Label("—");

    private final Button exportPublicButton = new Button("Export public key…");
    private final Button importPublicButton = new Button("Import public key…");
    private final Label originLabel = new Label("Generate a key pair or import a public key.");

    private final Button savePrivateButton = new Button("Save private key…");
    private final Button loadPrivateButton = new Button("Load private key…");
    private final Label privateKeyPathLabel = new Label("No private key loaded.");

    private final StatusBanner statusBanner = new StatusBanner();
    private final BusyPane busyPane;

    public KeyManagementView() {
        generateButton.getStyleClass().add(Theme.PRIMARY_BUTTON);

        fingerprintLabel.getStyleClass().add(Theme.CODE);
        fingerprintLabel.setWrapText(true);
        sessionStateLabel.getStyleClass().add(Theme.MUTED);
        sessionStateLabel.setWrapText(true);
        originLabel.getStyleClass().add(Theme.MUTED);
        originLabel.setWrapText(true);
        privateKeyPathLabel.getStyleClass().add(Theme.MUTED);
        privateKeyPathLabel.setWrapText(true);

        Card keyPairCard = new Card("Key pair");
        keyPairCard.add(generateButton, sessionStateLabel, fingerprintLabel);

        Card publicCard = new Card("Public key — shareable");
        Label publicHint = new Label(
                "Send this key to people who want to encrypt something for you. "
                        + "It cannot decrypt anything.");
        publicHint.getStyleClass().add(Theme.MUTED);
        publicHint.setWrapText(true);
        HBox publicActions = new HBox(10, exportPublicButton, importPublicButton);
        publicActions.setAlignment(Pos.CENTER_LEFT);
        publicCard.add(publicHint, publicActions, originLabel);

        Card privateCard = new Card("Private key — never share");
        Label privateHint = new Label(
                "The private key stays on this machine and is protected with your passphrase. "
                        + "It is never transmitted and never written unencrypted.");
        privateHint.getStyleClass().add(Theme.MUTED);
        privateHint.setWrapText(true);
        HBox privateActions = new HBox(10, savePrivateButton, loadPrivateButton);
        privateActions.setAlignment(Pos.CENTER_LEFT);
        privateCard.add(privateHint, privateActions, privateKeyPathLabel);

        VBox content = new VBox(16);
        content.setPadding(new Insets(24, 24, 32, 24));
        content.getChildren().addAll(
                new SectionHeader("Key Management",
                        "Generate an RSA-3072 key pair, share the public key, and keep the "
                                + "private key protected on disk."),
                keyPairCard,
                publicCard,
                privateCard,
                statusBanner);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        busyPane = new BusyPane(scroll);
        getChildren().add(busyPane);
    }

    public Button generateButton() {
        return generateButton;
    }

    public Label sessionStateLabel() {
        return sessionStateLabel;
    }

    public Label fingerprintLabel() {
        return fingerprintLabel;
    }

    public Button exportPublicButton() {
        return exportPublicButton;
    }

    public Button importPublicButton() {
        return importPublicButton;
    }

    public Label originLabel() {
        return originLabel;
    }

    public Button savePrivateButton() {
        return savePrivateButton;
    }

    public Button loadPrivateButton() {
        return loadPrivateButton;
    }

    public Label privateKeyPathLabel() {
        return privateKeyPathLabel;
    }

    public StatusBanner statusBanner() {
        return statusBanner;
    }

    public BusyPane busyPane() {
        return busyPane;
    }
}
