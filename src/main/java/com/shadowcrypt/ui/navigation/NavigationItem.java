package com.shadowcrypt.ui.navigation;

/** The four primary sections of the application. */
public enum NavigationItem {

    ENCRYPT("Encryption", "Encrypt & embed"),
    DECRYPT("Decryption", "Extract & decrypt"),
    ANALYZE("Image Analysis", "Measure image impact"),
    KEYS("Key Management", "RSA-3072 keys");

    private final String title;
    private final String subtitle;

    NavigationItem(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    public String title() {
        return title;
    }

    public String subtitle() {
        return subtitle;
    }
}
