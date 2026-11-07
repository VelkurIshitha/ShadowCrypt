package com.shadowcrypt.format.payload;

import com.shadowcrypt.config.PackageConstants;
import com.shadowcrypt.exception.MalformedPackageException;
import com.shadowcrypt.format.serialization.BigEndianWriter;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Serializes the inner DATA payload. Length fields count UTF-8 bytes, never Java
 * characters. Metadata never leaves the encrypted layer.
 */
public final class PayloadSerializer {

    private PayloadSerializer() {
    }

    public static byte[] serialize(InnerPayload payload) {
        Objects.requireNonNull(payload, "payload");

        byte[] fileName = payload.fileName().getBytes(StandardCharsets.UTF_8);
        byte[] mimeType = payload.mimeType().getBytes(StandardCharsets.UTF_8);
        byte[] data = payload.data();

        validateFileName(payload.fileName());
        if (fileName.length > PackageConstants.MAX_FILENAME_BYTES) {
            throw new MalformedPackageException("The file name is too long for the package format");
        }
        if (mimeType.length > PackageConstants.MAX_MIME_TYPE_BYTES) {
            throw new MalformedPackageException("The MIME type is too long for the package format");
        }

        BigEndianWriter writer = new BigEndianWriter()
                .writeBytes(PackageConstants.INNER_MAGIC)
                .writeByte(PackageConstants.INNER_VERSION)
                .writeByte(payload.type().code())
                .writeShort(fileName.length)
                .writeBytes(fileName)
                .writeShort(mimeType.length)
                .writeBytes(mimeType)
                .writeLong(data.length)
                .writeBytes(data);

        return writer.toByteArray();
    }

    private static void validateFileName(String fileName) {
        if (fileName.indexOf('/') >= 0 || fileName.indexOf('\\') >= 0 || fileName.indexOf('\0') >= 0) {
            throw new MalformedPackageException("File names must not contain path separators");
        }
    }
}
