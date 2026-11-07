package com.shadowcrypt.analysis.metrics;

/** Peak signal-to-noise ratio for 8-bit channels. */
public final class PsnrCalculator {

    public static final double MAX_CHANNEL_VALUE = 255.0;

    private PsnrCalculator() {
    }

    public static double calculate(double mse) {
        if (mse < 0) {
            throw new IllegalArgumentException("MSE must not be negative");
        }
        if (mse == 0.0) {
            return Double.POSITIVE_INFINITY;
        }
        return 10.0 * Math.log10((MAX_CHANNEL_VALUE * MAX_CHANNEL_VALUE) / mse);
    }
}
