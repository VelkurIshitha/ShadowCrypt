package com.shadowcrypt.app;

import javafx.application.Application;

/**
 * Plain entry point. Keeping a non-Application main class lets the shaded jar
 * launch JavaFX without the "JavaFX runtime components are missing" error.
 */
public final class AppLauncher {

    private AppLauncher() {
    }

    public static void main(String[] args) {
        Application.launch(ShadowCryptApplication.class, args);
    }
}
