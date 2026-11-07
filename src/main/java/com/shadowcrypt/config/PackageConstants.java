package com.shadowcrypt.config;

/**
 * Fixed layout values for the SHADOWCRYPT V1 binary package. All integers are
 * big-endian; these constants are referenced by the serializer and the parser so
 * the two can never disagree about a field width.
 */
public final class PackageConstants {

    public static final byte[] MAGIC = {0x53, 0x48, 0x44, 0x57}; // "SHDW"
    public static final byte[] INNER_MAGIC = {0x44, 0x41, 0x54, 0x41}; // "DATA"

    public static final int VERSION = 1;
    public static final int INNER_VERSION = 1;

    public static final int MAGIC_BYTES = 4;
    public static final int VERSION_BYTES = 1;
    public static final int FLAGS_BYTES = 1;
    public static final int TOTAL_LENGTH_BYTES = 4;
    public static final int WRAPPED_KEY_LENGTH_BYTES = 2;
    public static final int NONCE_LENGTH_BYTES = 1;
    public static final int CIPHERTEXT_LENGTH_BYTES = 4;

    public static final int BOOTSTRAP_HEADER_BYTES =
            MAGIC_BYTES + VERSION_BYTES + FLAGS_BYTES + TOTAL_LENGTH_BYTES
                    + WRAPPED_KEY_LENGTH_BYTES + NONCE_LENGTH_BYTES + CIPHERTEXT_LENGTH_BYTES;

    public static final int FLAGS_OFFSET = MAGIC_BYTES + VERSION_BYTES;
    public static final int TOTAL_LENGTH_OFFSET = FLAGS_OFFSET + FLAGS_BYTES;
    public static final int WRAPPED_KEY_LENGTH_OFFSET = TOTAL_LENGTH_OFFSET + TOTAL_LENGTH_BYTES;
    public static final int NONCE_LENGTH_OFFSET = WRAPPED_KEY_LENGTH_OFFSET + WRAPPED_KEY_LENGTH_BYTES;
    public static final int CIPHERTEXT_LENGTH_OFFSET = NONCE_LENGTH_OFFSET + NONCE_LENGTH_BYTES;

    public static final int KNOWN_FLAGS_MASK = 0x00;

    public static final int MAX_FILENAME_BYTES = 0xFFFF;
    public static final int MAX_MIME_TYPE_BYTES = 0xFFFF;

    public static final byte PAYLOAD_TYPE_TEXT = 0x01;
    public static final byte PAYLOAD_TYPE_FILE = 0x02;

    /**
     * Smallest possible package overhead before payload bytes: bootstrap header,
     * wrapped key, nonce, GCM tag, and the fixed inner-payload fields.
     */
    public static final int MINIMUM_PACKAGE_OVERHEAD_BYTES =
            BOOTSTRAP_HEADER_BYTES + CryptoConstants.RSA_WRAPPED_KEY_BYTES
                    + CryptoConstants.GCM_NONCE_BYTES + CryptoConstants.GCM_TAG_BYTES + 18;

    private PackageConstants() {
    }
}
