package com.shadowcrypt.format.payload;

import com.shadowcrypt.model.PayloadType;

import java.util.Objects;

/** Decrypted inner payload: what was actually encrypted, plus its metadata. */
public record InnerPayload(PayloadType type, String fileName, String mimeType, byte[] data) {

    public InnerPayload {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(fileName, "fileName");
        Objects.requireNonNull(mimeType, "mimeType");
        Objects.requireNonNull(data, "data");
        data = data.clone();
    }

    @Override
    public byte[] data() {
        return data.clone();
    }
}
