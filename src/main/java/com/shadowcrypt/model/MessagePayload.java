package com.shadowcrypt.model;

import java.util.Objects;

/** Plaintext to encrypt, together with the metadata stored inside the package. */
public record MessagePayload(PayloadType type, String fileName, String mimeType, byte[] data) {

    public MessagePayload {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(data, "data");
        fileName = fileName == null ? "" : fileName;
        mimeType = mimeType == null ? "" : mimeType;
        data = data.clone();
    }

    @Override
    public byte[] data() {
        return data.clone();
    }
}
