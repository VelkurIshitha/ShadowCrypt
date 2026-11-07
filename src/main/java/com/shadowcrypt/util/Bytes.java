package com.shadowcrypt.util;

import java.security.MessageDigest;
import java.util.Objects;

/** Small byte-array helpers used by serialization and fingerprint display. */
public final class Bytes {

    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    private Bytes() {
    }

    public static byte[] concat(byte[]... parts) {
        int total = 0;
        for (byte[] part : parts) {
            Objects.requireNonNull(part, "part");
            total += part.length;
        }
        byte[] result = new byte[total];
        int offset = 0;
        for (byte[] part : parts) {
            System.arraycopy(part, 0, result, offset, part.length);
            offset += part.length;
        }
        return result;
    }

    public static String toHex(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes");
        char[] out = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xFF;
            out[i * 2] = HEX_DIGITS[value >>> 4];
            out[i * 2 + 1] = HEX_DIGITS[value & 0x0F];
        }
        return new String(out);
    }

    public static String toColonHex(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes");
        StringBuilder builder = new StringBuilder(bytes.length * 3);
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) {
                builder.append(':');
            }
            int value = bytes[i] & 0xFF;
            builder.append(Character.toUpperCase(HEX_DIGITS[value >>> 4]));
            builder.append(Character.toUpperCase(HEX_DIGITS[value & 0x0F]));
        }
        return builder.toString();
    }

    public static boolean constantTimeEquals(byte[] left, byte[] right) {
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(right, "right");
        return MessageDigest.isEqual(left, right);
    }
}
