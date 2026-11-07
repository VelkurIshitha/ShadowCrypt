package com.shadowcrypt.ui.keys;

import com.shadowcrypt.config.AppConstants;
import com.shadowcrypt.exception.ShadowCryptException;
import com.shadowcrypt.service.KeyService;
import com.shadowcrypt.ui.components.Dialogs;
import com.shadowcrypt.ui.components.ErrorText;
import com.shadowcrypt.ui.components.PassphraseDialog;
import com.shadowcrypt.ui.main.AppState;
import com.shadowcrypt.ui.main.BackgroundTaskRunner;
import com.shadowcrypt.util.SecureWipe;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Locale;

/** Wires key generation, public exchange, and protected private-key storage. */
public final class KeyManagementController {

    private final KeyManagementView view;
    private final AppState state;
    private final KeyService keyService;

    private PrivateKey currentPrivateKey;

    public KeyManagementController(KeyManagementView view, AppState state, KeyService keyService) {
        this.view = view;
        this.state = state;
        this.keyService = keyService;

        view.generateButton().setOnAction(event -> generateKeyPair());
        view.exportPublicButton().setOnAction(event -> exportPublicKey());
        view.importPublicButton().setOnAction(event -> importPublicKey());
        view.savePrivateButton().setOnAction(event -> savePrivateKey());
        view.loadPrivateButton().setOnAction(event -> loadPrivateKey());

        state.activePublicKeyProperty().addListener((observable, oldValue, newValue) -> refresh());
        state.activePublicKeyOriginProperty().addListener((observable, oldValue, newValue) -> refresh());
        state.lastPrivateKeyFileProperty().addListener((observable, oldValue, newValue) -> refresh());
        refresh();
    }

    private void generateKeyPair() {
        setBusy(true, "Generating an RSA-3072 key pair…");

        Task<KeyPair> task = new Task<>() {
            @Override
            protected KeyPair call() {
                return keyService.generateKeyPair();
            }
        };

        BackgroundTaskRunner.run(task,
                pair -> {
                    currentPrivateKey = pair.getPrivate();
                    state.setActivePublicKey(pair.getPublic(), "Generated in this session");
                    setBusy(false, null);
                    view.statusBanner().success("RSA-3072 key pair generated. Save the private key "
                            + "with a passphrase before closing the application.");
                },
                error -> {
                    setBusy(false, null);
                    view.statusBanner().error(ErrorText.describe(error));
                });
    }

    private void exportPublicKey() {
        PublicKey publicKey = state.activePublicKey();
        if (publicKey == null) {
            view.statusBanner().warning("There is no public key to export yet.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export the public key");
        chooser.setInitialFileName("shadowcrypt-public" + AppConstants.PUBLIC_KEY_EXTENSION);
        File initialDirectory = initialKeyDirectory();
        if (initialDirectory != null) {
            chooser.setInitialDirectory(initialDirectory);
        }
        chooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("ShadowCrypt public key", "*.scpub"));

        File file = chooser.showSaveDialog(window());
        if (file == null) {
            return;
        }
        Path path = withExtension(file.toPath(), AppConstants.PUBLIC_KEY_EXTENSION);

        boolean replaceExisting = false;
        if (Files.exists(path) && !Dialogs.confirm(window(), "Replace public-key file?",
                "The public-key file already exists.", path + "\n\nReplace it?")) {
            view.statusBanner().info("Export canceled; the existing file was kept.");
            return;
        }
        replaceExisting = Files.exists(path);

        try {
            keyService.exportPublicKey(path, publicKey, replaceExisting);
            view.statusBanner().success("Public key exported to " + path
                    + ". This file is safe to share with senders.");
        } catch (ShadowCryptException e) {
            view.statusBanner().error(e.getMessage());
        }
    }

    private void importPublicKey() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import a public key");
        chooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("ShadowCrypt public key", "*.scpub"));
        File file = chooser.showOpenDialog(window());
        if (file == null) {
            return;
        }
        try {
            PublicKey imported = keyService.importPublicKey(file.toPath());
            currentPrivateKey = null;
            state.setActivePublicKey(imported, "Imported from " + file.getName());
            view.statusBanner().success("Public key imported. Fingerprint "
                    + keyService.fingerprint(imported));
        } catch (ShadowCryptException e) {
            view.statusBanner().error(e.getMessage());
        }
    }

    private void savePrivateKey() {
        if (currentPrivateKey == null) {
            view.statusBanner().warning(
                    "Generate a key pair or load a private key before saving one.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save the protected private key");
        chooser.setInitialFileName("shadowcrypt-private" + AppConstants.PRIVATE_KEY_EXTENSION);
        File initialDirectory = initialKeyDirectory();
        if (initialDirectory != null) {
            chooser.setInitialDirectory(initialDirectory);
        }
        chooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("ShadowCrypt private key", "*.scpriv"));

        File file = chooser.showSaveDialog(window());
        if (file == null) {
            return;
        }
        Path path = withExtension(file.toPath(), AppConstants.PRIVATE_KEY_EXTENSION);

        boolean replaceExisting = false;
        if (Files.exists(path) && !Dialogs.confirm(window(), "Replace private-key file?",
                "A private-key file already exists here.", path + "\n\nReplace it?")) {
            view.statusBanner().info("Save canceled; the existing key file was kept.");
            return;
        }
        replaceExisting = Files.exists(path);

        char[] passphrase = PassphraseDialog.request(window(), "Protect the private key", true);
        if (passphrase == null) {
            return;
        }

        PrivateKey privateKey = currentPrivateKey;
        boolean replace = replaceExisting;
        setBusy(true, "Protecting and saving the private key…");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    keyService.savePrivateKey(path, privateKey, passphrase, replace);
                    return null;
                } finally {
                    SecureWipe.wipe(passphrase);
                }
            }
        };

        BackgroundTaskRunner.run(task,
                ignored -> {
                    state.setLastPrivateKeyFile(path);
                    setBusy(false, null);
                    view.statusBanner().success("Private key saved with PBKDF2-HMAC-SHA256 "
                            + "protection to " + path);
                },
                error -> {
                    setBusy(false, null);
                    view.statusBanner().error(ErrorText.describe(error));
                });
    }

    private void loadPrivateKey() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load a protected private key");
        chooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("ShadowCrypt private key", "*.scpriv"));
        File file = chooser.showOpenDialog(window());
        if (file == null) {
            return;
        }

        char[] passphrase = PassphraseDialog.request(window(), "Unlock the private key", false);
        if (passphrase == null) {
            return;
        }

        Path path = file.toPath();
        setBusy(true, "Unlocking the private key…");

        Task<LoadedKey> task = new Task<>() {
            @Override
            protected LoadedKey call() {
                try {
                    PrivateKey privateKey = keyService.loadPrivateKey(path, passphrase);
                    PublicKey publicKey = keyService.derivePublicKey(privateKey);
                    return new LoadedKey(privateKey, publicKey);
                } finally {
                    SecureWipe.wipe(passphrase);
                }
            }
        };

        BackgroundTaskRunner.run(task,
                loadedKey -> {
                    currentPrivateKey = loadedKey.privateKey();
                    state.setActivePublicKey(loadedKey.publicKey(), "Loaded from " + path.getFileName());
                    state.setLastPrivateKeyFile(path);
                    setBusy(false, null);
                    view.statusBanner().success("Private key loaded. Public fingerprint "
                            + keyService.fingerprint(loadedKey.publicKey()));
                },
                error -> {
                    setBusy(false, null);
                    view.statusBanner().error(ErrorText.describe(error));
                });
    }

    private void setBusy(boolean busy, String message) {
        view.generateButton().setDisable(busy);
        view.exportPublicButton().setDisable(busy);
        view.importPublicButton().setDisable(busy);
        view.savePrivateButton().setDisable(busy);
        view.loadPrivateButton().setDisable(busy);
        if (busy) {
            view.busyPane().showIndeterminate(message);
        } else {
            view.busyPane().hideBusy();
            refresh();
        }
    }

    private void refresh() {
        PublicKey publicKey = state.activePublicKey();
        boolean hasPublicKey = publicKey != null;

        view.exportPublicButton().setDisable(!hasPublicKey);
        view.savePrivateButton().setDisable(currentPrivateKey == null);
        view.generateButton().setDisable(false);
        view.importPublicButton().setDisable(false);
        view.loadPrivateButton().setDisable(false);

        if (hasPublicKey) {
            view.sessionStateLabel().setText(currentPrivateKey == null
                    ? "Recipient public key only — no private key in this session."
                    : "Active key pair in this session.");
            view.fingerprintLabel().setText("SHA-256  " + keyService.fingerprint(publicKey));
            view.originLabel().setText(state.activePublicKeyOrigin().isBlank()
                    ? "Available for encryption." : state.activePublicKeyOrigin());
        } else {
            view.sessionStateLabel().setText("No key pair in this session.");
            view.fingerprintLabel().setText("—");
            view.originLabel().setText("Generate a key pair or import a public key.");
        }

        Path privateKeyPath = state.lastPrivateKeyFile();
        if (privateKeyPath != null) {
            view.privateKeyPathLabel().setText(privateKeyPath.toString());
        } else if (currentPrivateKey != null) {
            view.privateKeyPathLabel().setText(
                    "Not saved yet — the private key currently lives in memory only.");
        } else {
            view.privateKeyPathLabel().setText("No private key loaded.");
        }
    }

    private static File initialKeyDirectory() {
        Path directory = AppConstants.defaultKeyDirectory();
        try {
            Files.createDirectories(directory);
            return directory.toFile();
        } catch (IOException e) {
            return null;
        }
    }

    private static Path withExtension(Path path, String extension) {
        String name = path.getFileName().toString();
        if (name.toLowerCase(Locale.ROOT).endsWith(extension)) {
            return path;
        }
        return path.resolveSibling(name + extension);
    }

    private Window window() {
        return view.getScene() == null ? null : view.getScene().getWindow();
    }

    private record LoadedKey(PrivateKey privateKey, PublicKey publicKey) {
    }
}
