package com.shadowcrypt.exception;

/** Raised when a JCA operation cannot be completed. */
public class CryptoOperationException extends ShadowCryptException {

    public CryptoOperationException(String message) {
        super(message);
    }

    public CryptoOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
