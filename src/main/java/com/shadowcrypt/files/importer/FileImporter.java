package com.shadowcrypt.files.importer;

import com.shadowcrypt.config.AppConstants;
import com.shadowcrypt.exception.FileSafetyException;
import com.shadowcrypt.files.validation.MimeTypeDetector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/** Reads an input file with size and existence guards. Files are raw binary data. */
public final class FileImporter {

    private FileImporter() {
    }

    public record ImportedFile(Path source, String fileName, String mimeType, byte[] data) {

        public ImportedFile {
            Objects.requireNonNull(source, "source");
            Objects.requireNonNull(fileName, "fileName");
            Objects.requireNonNull(mimeType, "mimeType");
            Objects.requireNonNull(data, "data");
            data = data.clone();
        }

        @Override
        public byte[] data() {
            return data.clone();
        }
    }

    public static ImportedFile read(Path file) {
        return read(file, AppConstants.MAX_INPUT_FILE_BYTES);
    }

    public static ImportedFile read(Path file, long maxBytes) {
        Objects.requireNonNull(file, "file");
        if (maxBytes < 0) {
            throw new IllegalArgumentException("The size limit must not be negative");
        }
        try {
            if (!Files.isRegularFile(file)) {
                throw new FileSafetyException("The selected file does not exist or is not a regular file");
            }
            long size = Files.size(file);
            if (size > maxBytes) {
                throw new FileSafetyException("The selected file is " + size
                        + " bytes, which exceeds the safe limit of " + maxBytes + " bytes");
            }
            if (size > Integer.MAX_VALUE) {
                throw new FileSafetyException("The selected file is too large to process in memory");
            }
            byte[] data = Files.readAllBytes(file);
            String fileName = file.getFileName() != null ? file.getFileName().toString() : "file";
            return new ImportedFile(file, fileName, MimeTypeDetector.detect(file), data);
        } catch (IOException e) {
            throw new FileSafetyException("Unable to read the selected file: " + file, e);
        }
    }
}
