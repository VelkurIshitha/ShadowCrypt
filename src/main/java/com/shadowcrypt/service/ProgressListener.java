package com.shadowcrypt.service;

/** Reports workflow stages from background threads; the UI marshals to the FX thread. */
@FunctionalInterface
public interface ProgressListener {

    ProgressListener NONE = (progress, message) -> {
    };

    void report(double progress, String message);
}
