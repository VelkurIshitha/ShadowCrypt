package com.shadowcrypt.format.payload;

import com.shadowcrypt.config.PackageConstants;
import com.shadowcrypt.exception.MalformedPackageException;
import com.shadowcrypt.format.serialization.BigEndianReader;
import com.shadowcrypt.model.PayloadType;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/** Strict parser for the decrypted inner DATA payload. */
public final class PayloadParser {

    private PayloadParser() {
    }

    public static InnerPayload parse(byte[] plaintext) {
        Objects.requireNonNull(plaintext, "plaintext");
        BigEndianReader reader = new BigEndianReader(plaintext);

        byte[] magic = reader.readBytes(PackageConstants.INNER_MAGIC.length);
        if (!Arrays.equals(magic, PackageConstants.INNER_MAGIC)) {
            throw new MalformedPackageException("The decrypted payload has an invalid inner magic number");
        }

        int version = reader.readUnsignedByte();
        if (version != PackageConstants.INNER_VERSION) {
            throw new MalformedPackageException("Unsupported inner payload version: " + version);
        }

        int typeCode = reader.readUnsignedByte();
        PayloadType type = switch (typeCode) {
            case PackageConstants.PAYLOAD_TYPE_TEXT -> PayloadType.TEXT;
            case PackageConstants.PAYLOAD_TYPE_FILE -> PayloadType.FILE;
            default -> throw new MalformedPackageException("Unknown payload type: " + typeCode);
        };

        int fileNameLength = reader.readUnsignedShort();
        if (fileNameLength > reader.remaining()) {
            throw new MalformedPackageException("The payload file name length exceeds the remaining data");
        }
        String fileName = decodeUtf8(reader.readBytes(fileNameLength), "file name");

        int mimeLength = reader.readUnsignedShort();
        if (mimeLength > reader.remaining()) {
            throw new MalformedPackageException("The payload MIME length exceeds the remaining data");
        }
        String mimeType = decodeUtf8(reader.readBytes(mimeLength), "MIME type");

        long dataLength = reader.readLong();
        if (dataLength < 0 || dataLength > Integer.MAX_VALUE) {
            throw new MalformedPackageException("Invalid payload data length: " + dataLength);
        }
        if (dataLength != reader.remaining()) {
            throw new MalformedPackageException("The payload data length does not match the decrypted data");
        }
        byte[] data = reader.readBytes((int) dataLength);

        if (fileName.indexOf('/') >= 0 || fileName.indexOf('\\') >= 0 || fileName.indexOf('\0') >= 0) {
            throw new MalformedPackageException("The payload file name contains path separators");
        }

        return new InnerPayload(type, fileName, mimeType, data);
    }

    private static String decodeUtf8(byte[] bytes, String fieldName) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException e) {
            throw new MalformedPackageException("The payload " + fieldName + " is not valid UTF-8", e);
        }
    }
}
