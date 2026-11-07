package com.shadowcrypt.ui.components;

import java.util.Locale;

/** Shared human-readable formatting for sizes, percentages, and durations. */
public final class Formatters {

    private Formatters() {
    }

    public static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024L * 1024L) {
            return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
        }
        return String.format(Locale.ROOT, "%.1f MB", bytes / (1024.0 * 1024.0));
    }

    public static String formatPercent(double percent) {
        return String.format(Locale.ROOT, "%.2f%%", percent);
    }

    public static String formatDurationMillis(long nanos) {
        return String.format(Locale.ROOT, "%.1f ms", nanos / 1_000_000.0);
    }
}
