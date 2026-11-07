package com.shadowcrypt.exception;

/** Base type for every expected ShadowCrypt failure. */
public class ShadowCryptException extends RuntimeException {

    public ShadowCryptException(String message) {
        super(message);
    }

    public ShadowCryptException(String message, Throwable cause) {
        super(message, cause);
    }
}
