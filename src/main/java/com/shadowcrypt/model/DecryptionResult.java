package com.shadowcrypt.model;

import java.nio.file.Path;

/**
 * Outcome of a decryption run. Text payloads set {@code text}; file payloads set
 * {@code recoveredFile} after a safe export.
 */
public record DecryptionResult(
        PayloadType type,
        String text,
        Path recoveredFile,
        String fileName,
        String mimeType,
        String recoveredSha256,
        long packageBytes,
        long extractionNanos,
        long decryptionNanos) {
}
