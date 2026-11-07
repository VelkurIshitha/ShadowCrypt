package com.shadowcrypt.exception;

/** Raised when a protected private-key file cannot be written or read safely. */
public class KeyStorageException extends ShadowCryptException {

    public KeyStorageException(String message) {
        super(message);
    }

    public KeyStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
