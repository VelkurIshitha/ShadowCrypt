package com.shadowcrypt.exception;

/** Raised when LSB embedding or extraction cannot be performed safely. */
public class SteganographyException extends ShadowCryptException {

    public SteganographyException(String message) {
        super(message);
    }

    public SteganographyException(String message, Throwable cause) {
        super(message, cause);
    }
}
