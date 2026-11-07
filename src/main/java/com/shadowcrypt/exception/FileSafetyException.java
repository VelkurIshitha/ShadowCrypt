package com.shadowcrypt.exception;

/** Raised when untrusted file names or paths would be unsafe to write. */
public class FileSafetyException extends ShadowCryptException {

    public FileSafetyException(String message) {
        super(message);
    }

    public FileSafetyException(String message, Throwable cause) {
        super(message, cause);
    }
}
