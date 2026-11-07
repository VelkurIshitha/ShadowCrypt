package com.shadowcrypt.ui.encryption;

import com.shadowcrypt.config.AppConstants;
import com.shadowcrypt.config.PackageConstants;
import com.shadowcrypt.exception.ShadowCryptException;
import com.shadowcrypt.files.validation.MimeTypeDetector;
import com.shadowcrypt.image.models.CarrierImage;
import com.shadowcrypt.image.normalization.CarrierNormalizer;
import com.shadowcrypt.model.AnalysisReport;
import com.shadowcrypt.model.EncryptionRequest;
import com.shadowcrypt.model.EncryptionResult;
import com.shadowcrypt.model.PayloadType;
import com.shadowcrypt.service.AnalysisService;
import com.shadowcrypt.service.EncryptionService;
import com.shadowcrypt.service.KeyService;
import com.shadowcrypt.steganography.capacity.CapacityCalculator;
import com.shadowcrypt.ui.components.Dialogs;
import com.shadowcrypt.ui.components.ErrorText;
import com.shadowcrypt.ui.components.Formatters;
import com.shadowcrypt.ui.main.AppState;
import com.shadowcrypt.ui.main.BackgroundTaskRunner;
import com.shadowcrypt.ui.theme.Theme;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.PublicKey;
import java.util.List;
import java.util.Locale;

/** Wires the encryption screen to file pickers, capacity feedback, and services. */
public final class EncryptionController {

    private final EncryptionView view;
    private final AppState state;
    private final KeyService keyService;
    private final EncryptionService encryptionService;
    private final AnalysisService analysisService;

    private long carrierCapacityBytes = -1;
    private String carrierDimensions = "";

    public EncryptionController(EncryptionView view, AppState state, KeyService keyService,
                                EncryptionService encryptionService, AnalysisService analysisService) {
        this.view = view;
        this.state = state;
        this.keyService = keyService;
        this.encryptionService = encryptionService;
        this.analysisService = analysisService;

        view.carrierPicker().setOnBrowse(this::chooseCarrier);
        view.sourcePicker().setOnBrowse(this::chooseSourceFile);
        view.outputPicker().setOnBrowse(this::chooseOutput);
        view.importKeyButton().setOnAction(event -> importPublicKey());
        view.demoButton().setOnAction(event -> loadDemoPreset());
        view.encryptButton().setOnAction(event -> encrypt());
        view.textInput().textProperty().addListener((observable, oldValue, newValue) ->
                updateCapacityIndicator());
        view.setOnModeChanged(this::updateCapacityIndicator);

        wireGallery();

        state.activePublicKeyProperty().addListener((observable, oldValue, newValue) ->
                refreshKeyDisplay());
        state.activePublicKeyOriginProperty().addListener((observable, oldValue, newValue) ->
                refreshKeyDisplay());
        refreshKeyDisplay();
        updateCapacityIndicator();
    }

    private void wireGallery() {
        List<Button> buttons = view.galleryButtons();
        for (int index = 0; index < buttons.size(); index++) {
            String resource = AppConstants.BUNDLED_CARRIER_RESOURCES[index];
            buttons.get(index).setOnAction(event -> loadBundledCarrier(resource));
        }
    }

    private void chooseCarrier() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a PNG carrier image");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG images", "*.png"));
        File file = chooser.showOpenDialog(window());
        if (file != null) {
            applyCarrier(file.toPath());
        }
    }

    private void chooseSourceFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose the file to encrypt");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "*.*"));
        File file = chooser.showOpenDialog(window());
        if (file != null) {
            applySource(file.toPath());
        }
    }

    private void chooseOutput() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save the stego PNG");
        chooser.setInitialFileName("stego.png");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG images", "*.png"));
        File file = chooser.showSaveDialog(window());
        if (file == null) {
            return;
        }
        Path output = file.toPath();
        String name = output.getFileName().toString();
        if (!name.toLowerCase(Locale.ROOT).endsWith(".png")) {
            output = output.resolveSibling(name + ".png");
        }
        view.outputPicker().setPath(output);
    }

    private void applyCarrier(Path path) {
        try {
            CarrierImage carrier = CarrierNormalizer.load(path);
            view.carrierPicker().setPath(path);
            view.carrierPreview().setImage(carrier.image(), path.getFileName() + " • "
                    + carrier.width() + "×" + carrier.height());
            carrierCapacityBytes = CapacityCalculator.capacityBytes(carrier.width(), carrier.height());
            carrierDimensions = carrier.width() + "×" + carrier.height();
            view.statusBanner().hide();
            updateCapacityIndicator();
        } catch (ShadowCryptException e) {
            view.carrierPicker().setPath(path);
            view.carrierPreview().clear("Unsupported carrier");
            carrierCapacityBytes = -1;
            carrierDimensions = "";
            view.capacityLabel().setText("No valid carrier image selected.");
            view.capacityLabel().getStyleClass().setAll(Theme.ERROR_TEXT);
            view.statusBanner().error(e.getMessage());
        }
    }

    private void applySource(Path path) {
        view.sourcePicker().setPath(path);
        try {
            long size = Files.size(path);
            view.fileInfoLabel().setText(path.getFileName() + " • "
                    + Formatters.formatBytes(size) + " • " + MimeTypeDetector.detect(path));
        } catch (IOException e) {
            view.fileInfoLabel().setText(path.getFileName() + " • size unavailable");
        }
        updateCapacityIndicator();
    }

    private void loadBundledCarrier(String resource) {
        try {
            Path file = materializeResource(resource, ".png");
            applyCarrier(file);
        } catch (IOException e) {
            view.statusBanner().error("Unable to load the bundled carrier: " + e.getMessage());
        }
    }

    private void importPublicKey() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import a recipient public key");
        chooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("ShadowCrypt public key", "*.scpub"));
        File file = chooser.showOpenDialog(window());
        if (file == null) {
            return;
        }
        try {
            PublicKey publicKey = keyService.importPublicKey(file.toPath());
            state.setActivePublicKey(publicKey, "Imported from " + file.getName());
            view.statusBanner().success("Recipient public key imported. Check the fingerprint below.");
        } catch (ShadowCryptException e) {
            view.statusBanner().error(e.getMessage());
        }
    }

    private void refreshKeyDisplay() {
        PublicKey publicKey = state.activePublicKey();
        if (publicKey == null) {
            view.fingerprintLabel().setText("No recipient public key selected");
            view.keyOriginLabel().setText(
                    "Generate or import a key in Key Management, or import a .scpub file here.");
            return;
        }
        view.fingerprintLabel().setText("SHA-256  " + keyService.fingerprint(publicKey));
        view.keyOriginLabel().setText(state.activePublicKeyOrigin());
    }

    private void updateCapacityIndicator() {
        if (carrierCapacityBytes < 0) {
            view.capacityLabel().setText("Select a PNG carrier image to see its capacity.");
            view.capacityLabel().getStyleClass().setAll(Theme.MUTED);
            return;
        }
        long payloadBytes = estimatePayloadBytes();
        long minimumPackage = payloadBytes + PackageConstants.MINIMUM_PACKAGE_OVERHEAD_BYTES;
        boolean tooLarge = minimumPackage > carrierCapacityBytes;

        String message = "Payload: " + Formatters.formatBytes(payloadBytes)
                + "  •  carrier capacity: " + Formatters.formatBytes(carrierCapacityBytes)
                + " (" + carrierDimensions + ")"
                + "  •  minimum overhead: "
                + Formatters.formatBytes(PackageConstants.MINIMUM_PACKAGE_OVERHEAD_BYTES);
        if (tooLarge) {
            message += "  •  payload will not fit";
        }
        view.capacityLabel().setText(message);
        view.capacityLabel().getStyleClass().setAll(tooLarge ? Theme.WARNING_TEXT : Theme.MUTED);
    }

    private long estimatePayloadBytes() {
        if (view.isTextMode()) {
            String text = view.textInput().getText();
            return text == null ? 0 : text.getBytes(StandardCharsets.UTF_8).length;
        }
        Path source = view.sourcePicker().getPath();
        if (source == null) {
            return 0;
        }
        try {
            return Files.size(source);
        } catch (IOException e) {
            return 0;
        }
    }

    private void encrypt() {
        Path carrierPath = view.carrierPicker().getPath();
        Path outputPath = view.outputPicker().getPath();
        PublicKey publicKey = state.activePublicKey();

        if (publicKey == null) {
            view.statusBanner().warning(
                    "Select a recipient public key first (Key Management or Import public key).");
            return;
        }
        if (carrierPath == null) {
            view.statusBanner().warning("Choose a PNG carrier image.");
            return;
        }
        if (outputPath == null) {
            view.statusBanner().warning("Choose where to save the stego PNG.");
            return;
        }

        boolean textMode = view.isTextMode();
        String text = view.textInput().getText();
        Path sourceFile = view.sourcePicker().getPath();
        if (textMode && (text == null || text.isEmpty())) {
            view.statusBanner().warning("Enter the text to encrypt.");
            return;
        }
        if (!textMode && sourceFile == null) {
            view.statusBanner().warning("Choose the file to encrypt.");
            return;
        }

        boolean replaceExisting = false;
        if (Files.exists(outputPath)) {
            boolean confirmed = Dialogs.confirm(window(), "Replace output image?",
                    "The output image already exists.",
                    outputPath + "\n\nReplace this file?");
            if (!confirmed) {
                return;
            }
            replaceExisting = true;
        }

        EncryptionRequest request = new EncryptionRequest(
                textMode ? PayloadType.TEXT : PayloadType.FILE,
                textMode ? text : null,
                textMode ? null : sourceFile,
                carrierPath,
                publicKey,
                outputPath,
                replaceExisting);

        runEncryption(request);
    }

    private void runEncryption(EncryptionRequest request) {
        view.encryptButton().setDisable(true);
        view.statusBanner().hide();
        view.busyPane().showIndeterminate("Encrypting and embedding…");

        Task<EncryptionResult> task = new Task<>() {
            @Override
            protected EncryptionResult call() {
                return encryptionService.encrypt(request, (progress, message) ->
                        Platform.runLater(() -> view.busyPane().showProgress(progress, message)));
            }
        };

        BackgroundTaskRunner.run(task,
                result -> {
                    view.busyPane().hideBusy();
                    view.encryptButton().setDisable(false);
                    handleSuccess(result);
                },
                error -> {
                    view.busyPane().hideBusy();
                    view.encryptButton().setDisable(false);
                    view.statusBanner().error(ErrorText.describe(error));
                });
    }

    private void handleSuccess(EncryptionResult result) {
        AnalysisReport report = analysisService.analyze(result);
        state.lastEncryptionProperty().set(result);
        state.lastAnalysisProperty().set(report);

        view.outputPicker().setPath(result.stegoFile());
        view.carrierPreview().setImage(result.stegoImage(), "Stego image • "
                + result.width() + "×" + result.height() + " • "
                + Formatters.formatBytes(result.stegoPngBytes()));
        view.statusBanner().success("Stego PNG written to " + result.stegoFile()
                + ". Package " + Formatters.formatBytes(result.packageBytes())
                + " (" + Formatters.formatPercent(result.capacityUtilizationPercent())
                + " of carrier capacity). Analysis updated.");
    }

    private void loadDemoPreset() {
        if (state.activePublicKey() == null) {
            view.statusBanner().warning(
                    "Generate or import a recipient public key before loading the demo.");
            return;
        }
        try {
            Path carrierFile = materializeResource(AppConstants.DEMO_CARRIER_RESOURCE, ".png");
            String sampleText = readTextResource(AppConstants.DEMO_SAMPLE_RESOURCE);

            view.selectTextMode();
            view.setText(sampleText);
            applyCarrier(carrierFile);
            view.outputPicker().setPath(
                    Path.of(System.getProperty("java.io.tmpdir"), "shadowcrypt-demo-stego.png"));

            view.statusBanner().info(
                    "Demo preset loaded: bundled carrier and sample text. Press Encrypt & Embed.");
        } catch (IOException e) {
            view.statusBanner().error("Unable to load the demo preset: " + e.getMessage());
        }
    }

    private static Path materializeResource(String resource, String suffix) throws IOException {
        try (InputStream input = EncryptionController.class.getResourceAsStream(resource)) {
            if (input == null) {
                throw new IOException("Bundled resource not found: " + resource);
            }
            Path file = Files.createTempFile("shadowcrypt-asset", suffix);
            Files.copy(input, file, StandardCopyOption.REPLACE_EXISTING);
            file.toFile().deleteOnExit();
            return file;
        }
    }

    private static String readTextResource(String resource) throws IOException {
        try (InputStream input = EncryptionController.class.getResourceAsStream(resource)) {
            if (input == null) {
                throw new IOException("Bundled resource not found: " + resource);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private Window window() {
        return view.getScene() == null ? null : view.getScene().getWindow();
    }
}
