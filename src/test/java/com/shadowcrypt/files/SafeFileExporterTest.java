package com.shadowcrypt.files;

import com.shadowcrypt.exception.FileSafetyException;
import com.shadowcrypt.files.exporter.SafeFileExporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SafeFileExporterTest {

    @TempDir
    Path tempDirectory;

    @Test
    void writesInsideSelectedDirectoryWithSanitizedName() throws IOException {
        Path directory = tempDirectory.resolve("recovered");
        byte[] data = "recovered contents".getBytes();

        Path written = SafeFileExporter.write(directory, "../escape.txt", data, false);

        assertEquals(directory.toAbsolutePath().normalize(), written.getParent());
        assertEquals("escape.txt", written.getFileName().toString());
        assertArrayEquals(data, Files.readAllBytes(written));
    }

    @Test
    void createsMissingDirectory() {
        Path directory = tempDirectory.resolve("a").resolve("b");

        Path written = SafeFileExporter.write(directory, "file.bin", new byte[]{1, 2, 3}, false);

        assertTrue(Files.isRegularFile(written));
    }

    @Test
    void refusesToOverwriteWithoutPermission() {
        Path directory = tempDirectory.resolve("out");
        SafeFileExporter.write(directory, "note.txt", new byte[]{1}, false);

        assertThrows(FileSafetyException.class,
                () -> SafeFileExporter.write(directory, "note.txt", new byte[]{2}, false));
    }

    @Test
    void replacesWhenPermissionIsGiven() throws IOException {
        Path directory = tempDirectory.resolve("out");
        SafeFileExporter.write(directory, "note.txt", new byte[]{1}, false);

        Path written = SafeFileExporter.write(directory, "note.txt", new byte[]{2, 3}, true);

        assertArrayEquals(new byte[]{2, 3}, Files.readAllBytes(written));
    }

    @Test
    void fallsBackForInvalidNames() {
        Path directory = tempDirectory.resolve("out");

        Path written = SafeFileExporter.write(directory, "..", new byte[]{0}, false);

        assertEquals("recovered-file", written.getFileName().toString());
    }
}
