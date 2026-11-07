package com.shadowcrypt.config;

/**
 * Centralized cryptographic parameters. Changing a value here changes the whole
 * application, so these are treated as part of the file formats.
 */
public final class CryptoConstants {

    public static final String AES_ALGORITHM = "AES";
    public static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    public static final int AES_KEY_BITS = 256;
    public static final int AES_KEY_BYTES = AES_KEY_BITS / 8;

    public static final int GCM_NONCE_BYTES = 12;
    public static final int GCM_TAG_BITS = 128;
    public static final int GCM_TAG_BYTES = GCM_TAG_BITS / 8;

    public static final String RSA_ALGORITHM = "RSA";
    public static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    public static final int RSA_KEY_BITS = 3072;
    public static final int RSA_WRAPPED_KEY_BYTES = 384;
    public static final String OAEP_DIGEST = "SHA-256";
    public static final String OAEP_MGF1_DIGEST = "SHA-256";
    public static final String OAEP_MGF1_PARAMETER_NAME = "MGF1";

    public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    public static final int PBKDF2_ITERATIONS = 600_000;
    public static final int PBKDF2_SALT_BYTES = 16;

    public static final String SHA_256 = "SHA-256";

    public static final byte PRIVATE_KEY_FILE_VERSION = 1;
    public static final byte PRIVATE_KEY_KDF_PBKDF2_SHA256 = 1;

    private CryptoConstants() {
    }
}
