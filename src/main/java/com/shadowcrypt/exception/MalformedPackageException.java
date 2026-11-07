package com.shadowcrypt.exception;

/** The extracted bytes do not form a valid, safe SHADOWCRYPT package. */
public class MalformedPackageException extends ShadowCryptException {

    public MalformedPackageException(String message) {
        super(message);
    }

    public MalformedPackageException(String message, Throwable cause) {
        super(message, cause);
    }
}
