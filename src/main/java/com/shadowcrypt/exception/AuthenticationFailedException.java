package com.shadowcrypt.exception;

/** Authentication failed: wrong key, wrong passphrase, or tampered data. */
public class AuthenticationFailedException extends ShadowCryptException {

    public AuthenticationFailedException(String message) {
        super(message);
    }

    public AuthenticationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
