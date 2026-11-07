package com.shadowcrypt.config;

import java.nio.file.Path;

/** Application-wide constants that are not cryptographic parameters. */
public final class AppConstants {

    public static final String APP_NAME = "ShadowCrypt";

    public static final double WINDOW_WIDTH = 1320;
    public static final double WINDOW_HEIGHT = 800;
    public static final double MIN_WINDOW_WIDTH = 1100;
    public static final double MIN_WINDOW_HEIGHT = 680;

    public static final int HEATMAP_AMPLIFICATION = 16;

    public static final String PRIVATE_KEY_EXTENSION = ".scpriv";
    public static final String PUBLIC_KEY_EXTENSION = ".scpub";

    public static final long MAX_INPUT_FILE_BYTES = 256L * 1024L * 1024L;
    public static final int CHANNELS_PER_PIXEL = 3;
    public static final int OPAQUE_WHITE_ARGB = 0xFFFFFFFF;

    public static final String DEMO_CARRIER_RESOURCE = "/images/carriers/carrier-blue.png";
    public static final String DEMO_SAMPLE_RESOURCE = "/images/samples/welcome.txt";

    public static final String[] BUNDLED_CARRIER_RESOURCES = {
            "/images/carriers/carrier-blue.png",
            "/images/carriers/carrier-texture.png",
            "/images/carriers/carrier-warm.png"
    };

    public static final String[] BUNDLED_CARRIER_LABELS = {
            "Bundled: Blue",
            "Bundled: Texture",
            "Bundled: Warm"
    };

    private AppConstants() {
    }

    public static Path defaultKeyDirectory() {
        return Path.of(System.getProperty("user.home"), ".shadowcrypt", "keys");
    }
}
