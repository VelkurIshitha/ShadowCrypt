package com.shadowcrypt.model;

import com.shadowcrypt.config.PackageConstants;

/** Kind of data carried inside an encrypted package. */
public enum PayloadType {

    TEXT(PackageConstants.PAYLOAD_TYPE_TEXT),
    FILE(PackageConstants.PAYLOAD_TYPE_FILE);

    private final byte code;

    PayloadType(byte code) {
        this.code = code;
    }

    public byte code() {
        return code;
    }
}
