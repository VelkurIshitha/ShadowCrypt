package com.shadowcrypt.files.exporter;

import com.shadowcrypt.exception.FileSafetyException;
import com.shadowcrypt.files.validation.FileNameSanitizer;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * Writes recovered data only inside the user-selected directory, using a sanitized
 * plain file name and an atomic write. Existing files are never replaced silently.
 */
public final class SafeFileExporter {

    private SafeFileExporter() {
    }

    public static Path write(Path directory, String untrustedFileName, byte[] data, boolean replaceExisting) {
        Objects.requireNonNull(directory, "directory");
        Objects.requireNonNull(data, "data");

        Path base = directory.toAbsolutePath().normalize();
        String safeName = FileNameSanitizer.sanitize(untrustedFileName);
        Path target = base.resolve(safeName).normalize();

        if (target.getParent() == null || !target.getParent().equals(base)) {
            throw new FileSafetyException("The recovered file name escapes the selected directory");
        }

        try {
            Files.createDirectories(base);
            if (!replaceExisting && Files.exists(target)) {
                throw new FileSafetyException("A file already exists at " + target);
            }
            Path temp = Files.createTempFile(base, ".shadowcrypt-export", ".tmp");
            try {
                Files.write(temp, data);
                try {
                    Files.move(temp, target,
                            StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(temp);
            }
            return target;
        } catch (IOException e) {
            throw new FileSafetyException("Unable to write the recovered file: " + target, e);
        }
    }
}
