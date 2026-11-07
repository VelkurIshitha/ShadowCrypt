package com.shadowcrypt.util;

import java.util.Arrays;

/** Best-effort clearing of in-memory secret material. */
public final class SecureWipe {

    private SecureWipe() {
    }

    public static void wipe(byte[] secret) {
        if (secret != null) {
            Arrays.fill(secret, (byte) 0);
        }
    }

    public static void wipe(char[] secret) {
        if (secret != null) {
            Arrays.fill(secret, '\0');
        }
    }
}
