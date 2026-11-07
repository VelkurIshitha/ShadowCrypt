package com.shadowcrypt.files.validation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/** Best-effort MIME detection with a safe binary fallback. */
public final class MimeTypeDetector {

    public static final String FALLBACK_MIME_TYPE = "application/octet-stream";

    private MimeTypeDetector() {
    }

    public static String detect(Path file) {
        Objects.requireNonNull(file, "file");
        try {
            String detected = Files.probeContentType(file);
            if (detected != null && !detected.isBlank()) {
                return detected;
            }
        } catch (IOException e) {
            return FALLBACK_MIME_TYPE;
        }
        return FALLBACK_MIME_TYPE;
    }
}
