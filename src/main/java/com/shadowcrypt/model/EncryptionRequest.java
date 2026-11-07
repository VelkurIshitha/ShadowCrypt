package com.shadowcrypt.model;

import java.nio.file.Path;
import java.security.PublicKey;
import java.util.Objects;

/**
 * Everything needed to run one encryption. For {@link PayloadType#TEXT} the text is
 * used and {@code sourceFile} is null; for {@link PayloadType#FILE} the reverse.
 */
public record EncryptionRequest(
        PayloadType type,
        String text,
        Path sourceFile,
        Path carrierImage,
        PublicKey recipientPublicKey,
        Path outputFile,
        boolean replaceExisting) {

    public EncryptionRequest {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(carrierImage, "carrierImage");
        Objects.requireNonNull(recipientPublicKey, "recipientPublicKey");
        Objects.requireNonNull(outputFile, "outputFile");
        if (type == PayloadType.TEXT) {
            Objects.requireNonNull(text, "text");
            if (sourceFile != null) {
                throw new IllegalArgumentException("A text request must not carry a source file");
            }
        } else {
            Objects.requireNonNull(sourceFile, "sourceFile");
        }
    }
}
