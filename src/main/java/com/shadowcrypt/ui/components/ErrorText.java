package com.shadowcrypt.ui.components;

/** User-safe error text without exposing stack traces or secrets. */
public final class ErrorText {

    private ErrorText() {
    }

    public static String describe(Throwable error) {
        if (error == null) {
            return "The operation failed.";
        }
        String message = error.getMessage();
        if (message != null && !message.isBlank()) {
            return message;
        }
        return error.getClass().getSimpleName();
    }
}
