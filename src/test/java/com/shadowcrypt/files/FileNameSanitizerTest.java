package com.shadowcrypt.files;

import com.shadowcrypt.files.validation.FileNameSanitizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileNameSanitizerTest {

    @Test
    void stripsDirectoryComponentFromTraversalNames() {
        assertEquals("evil.txt", FileNameSanitizer.sanitize("../evil.txt"));
        assertEquals("evil.txt", FileNameSanitizer.sanitize("..\\evil.txt"));
        assertEquals("evil.txt", FileNameSanitizer.sanitize("C:\\Windows\\evil.txt"));
        assertEquals("evil.txt", FileNameSanitizer.sanitize("/tmp/evil.txt"));
    }

    @Test
    void fallsBackForEmptyAndDotNames() {
        assertEquals(FileNameSanitizer.FALLBACK_NAME, FileNameSanitizer.sanitize(null));
        assertEquals(FileNameSanitizer.FALLBACK_NAME, FileNameSanitizer.sanitize(""));
        assertEquals(FileNameSanitizer.FALLBACK_NAME, FileNameSanitizer.sanitize("   "));
        assertEquals(FileNameSanitizer.FALLBACK_NAME, FileNameSanitizer.sanitize("."));
        assertEquals(FileNameSanitizer.FALLBACK_NAME, FileNameSanitizer.sanitize(".."));
        assertEquals(FileNameSanitizer.FALLBACK_NAME, FileNameSanitizer.sanitize("/"));
    }

    @Test
    void replacesIllegalCharacters() {
        assertEquals("a_b_c.txt", FileNameSanitizer.sanitize("a:b|c.txt"));
        assertEquals("report_.pdf", FileNameSanitizer.sanitize("report?.pdf"));
        assertEquals("name_.txt", FileNameSanitizer.sanitize("name*.txt"));
    }

    @Test
    void removesTrailingDotsAndSpaces() {
        assertEquals("report.txt", FileNameSanitizer.sanitize("report.txt..."));
        assertEquals("report.txt", FileNameSanitizer.sanitize("report.txt   "));
    }

    @Test
    void avoidsReservedWindowsNames() {
        assertEquals("_CON", FileNameSanitizer.sanitize("CON"));
        assertEquals("_con.txt", FileNameSanitizer.sanitize("con.txt"));
        assertEquals("_LPT1.log", FileNameSanitizer.sanitize("LPT1.log"));
    }

    @Test
    void preservesUnicodeNames() {
        assertEquals("résumé-日本語.pdf", FileNameSanitizer.sanitize("résumé-日本語.pdf"));
    }

    @Test
    void truncatesOverlongNamesButKeepsTheExtension() {
        String longName = "a".repeat(500) + ".txt";

        String sanitized = FileNameSanitizer.sanitize(longName);

        assertEquals(200, sanitized.length());
        assertTrue(sanitized.endsWith(".txt"));
    }
}
