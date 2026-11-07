package com.shadowcrypt.ui.main;

import com.shadowcrypt.model.AnalysisReport;
import com.shadowcrypt.model.EncryptionResult;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.nio.file.Path;
import java.security.PublicKey;

/** Shared, observable UI state that connects the four sections. */
public final class AppState {

    private final ObjectProperty<PublicKey> activePublicKey = new SimpleObjectProperty<>();
    private final StringProperty activePublicKeyOrigin = new SimpleStringProperty("");
    private final ObjectProperty<Path> lastPrivateKeyFile = new SimpleObjectProperty<>();
    private final ObjectProperty<EncryptionResult> lastEncryption = new SimpleObjectProperty<>();
    private final ObjectProperty<AnalysisReport> lastAnalysis = new SimpleObjectProperty<>();

    public ObjectProperty<PublicKey> activePublicKeyProperty() {
        return activePublicKey;
    }

    public StringProperty activePublicKeyOriginProperty() {
        return activePublicKeyOrigin;
    }

    public ObjectProperty<Path> lastPrivateKeyFileProperty() {
        return lastPrivateKeyFile;
    }

    public ObjectProperty<EncryptionResult> lastEncryptionProperty() {
        return lastEncryption;
    }

    public ObjectProperty<AnalysisReport> lastAnalysisProperty() {
        return lastAnalysis;
    }

    public PublicKey activePublicKey() {
        return activePublicKey.get();
    }

    public String activePublicKeyOrigin() {
        return activePublicKeyOrigin.get();
    }

    public void setActivePublicKey(PublicKey publicKey, String origin) {
        activePublicKey.set(publicKey);
        activePublicKeyOrigin.set(origin == null ? "" : origin);
    }

    public Path lastPrivateKeyFile() {
        return lastPrivateKeyFile.get();
    }

    public void setLastPrivateKeyFile(Path path) {
        lastPrivateKeyFile.set(path);
    }
}
