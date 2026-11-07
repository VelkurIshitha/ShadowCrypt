package com.shadowcrypt.format.serialization;

import com.shadowcrypt.exception.MalformedPackageException;

import java.util.Arrays;
import java.util.Objects;

/** Bounded big-endian reader. Every overrun becomes a malformed-package error. */
public final class BigEndianReader {

    private final byte[] data;
    private final int limit;
    private int position;

    public BigEndianReader(byte[] data) {
        this(data, 0, Objects.requireNonNull(data, "data").length);
    }

    public BigEndianReader(byte[] data, int offset, int length) {
        Objects.requireNonNull(data, "data");
        if (offset < 0 || length < 0 || offset + length > data.length) {
            throw new IllegalArgumentException("Invalid reader window");
        }
        this.data = data;
        this.position = offset;
        this.limit = offset + length;
    }

    public int readUnsignedByte() {
        require(1);
        return data[position++] & 0xFF;
    }

    public int readUnsignedShort() {
        require(2);
        int value = ((data[position] & 0xFF) << 8) | (data[position + 1] & 0xFF);
        position += 2;
        return value;
    }

    public int readInt() {
        require(4);
        int value = ((data[position] & 0xFF) << 24)
                | ((data[position + 1] & 0xFF) << 16)
                | ((data[position + 2] & 0xFF) << 8)
                | (data[position + 3] & 0xFF);
        position += 4;
        return value;
    }

    public long readLong() {
        require(8);
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | (data[position + i] & 0xFFL);
        }
        position += 8;
        return value;
    }

    public byte[] readBytes(int length) {
        if (length < 0) {
            throw new MalformedPackageException("Negative field length: " + length);
        }
        require(length);
        byte[] value = Arrays.copyOfRange(data, position, position + length);
        position += length;
        return value;
    }

    public int remaining() {
        return limit - position;
    }

    public int position() {
        return position;
    }

    public int limit() {
        return limit;
    }

    private void require(int length) {
        if (length > remaining()) {
            throw new MalformedPackageException("The package is truncated: needed " + length
                    + " bytes but only " + remaining() + " remain");
        }
    }
}
