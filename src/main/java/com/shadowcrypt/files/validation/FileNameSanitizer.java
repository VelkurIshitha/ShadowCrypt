package com.shadowcrypt.files.validation;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Turns untrusted name metadata into a single safe file name. Directory components
 * are discarded, illegal characters replaced, reserved Windows names avoided, and
 * the result is always a plain name with no path separators.
 */
public final class FileNameSanitizer {

    public static final String FALLBACK_NAME = "recovered-file";

    private static final int MAX_NAME_LENGTH = 200;
    private static final Pattern ILLEGAL_CHARACTERS =
            Pattern.compile("[<>:\"/\\\\|?*\\p{Cntrl}]");
    private static final Pattern TRAILING_DOTS_OR_SPACES = Pattern.compile("[. ]+$");

    private static final Set<String> RESERVED_NAMES = Set.of(
            "CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9");

    private FileNameSanitizer() {
    }

    public static String sanitize(String untrustedName) {
        if (untrustedName == null) {
            return FALLBACK_NAME;
        }
        String name = untrustedName.strip();

        int lastSeparator = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (lastSeparator >= 0) {
            name = name.substring(lastSeparator + 1);
        }

        name = ILLEGAL_CHARACTERS.matcher(name).replaceAll("_");
        name = TRAILING_DOTS_OR_SPACES.matcher(name).replaceAll("");
        name = name.strip();

        if (name.isEmpty() || name.equals(".") || name.equals("..")) {
            return FALLBACK_NAME;
        }
        if (name.length() > MAX_NAME_LENGTH) {
            name = truncate(name);
        }

        String baseName = name.contains(".") ? name.substring(0, name.indexOf('.')) : name;
        if (RESERVED_NAMES.contains(baseName.toUpperCase(Locale.ROOT))) {
            name = "_" + name;
        }
        return name;
    }

    private static String truncate(String name) {
        int lastDot = name.lastIndexOf('.');
        String extension = (lastDot > 0 && name.length() - lastDot <= 16) ? name.substring(lastDot) : "";
        int keep = MAX_NAME_LENGTH - extension.length();
        return name.substring(0, Math.max(1, keep)) + extension;
    }
}
