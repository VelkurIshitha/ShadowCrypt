package com.shadowcrypt.model;

import java.nio.file.Path;
import java.util.Objects;

/** Inputs for one decryption run. The passphrase array is copied defensively. */
public record DecryptionRequest(
        Path stegoImage,
        Path privateKeyFile,
        char[] passphrase,
        Path outputDirectory,
        boolean replaceExisting) {

    public DecryptionRequest {
        Objects.requireNonNull(stegoImage, "stegoImage");
        Objects.requireNonNull(privateKeyFile, "privateKeyFile");
        Objects.requireNonNull(passphrase, "passphrase");
        Objects.requireNonNull(outputDirectory, "outputDirectory");
        passphrase = passphrase.clone();
    }

    @Override
    public char[] passphrase() {
        return passphrase.clone();
    }
}
