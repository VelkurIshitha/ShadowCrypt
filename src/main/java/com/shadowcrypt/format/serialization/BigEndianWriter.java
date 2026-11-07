package com.shadowcrypt.format.serialization;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

/** Minimal big-endian writer for the ShadowCrypt binary formats. */
public final class BigEndianWriter {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    public BigEndianWriter writeByte(int value) {
        if (value < 0 || value > 0xFF) {
            throw new IllegalArgumentException("Byte value out of range: " + value);
        }
        out.write(value);
        return this;
    }

    public BigEndianWriter writeShort(int value) {
        if (value < 0 || value > 0xFFFF) {
            throw new IllegalArgumentException("Unsigned short value out of range: " + value);
        }
        out.write((value >>> 8) & 0xFF);
        out.write(value & 0xFF);
        return this;
    }

    public BigEndianWriter writeInt(int value) {
        out.write((value >>> 24) & 0xFF);
        out.write((value >>> 16) & 0xFF);
        out.write((value >>> 8) & 0xFF);
        out.write(value & 0xFF);
        return this;
    }

    public BigEndianWriter writeLong(long value) {
        out.write((int) (value >>> 56) & 0xFF);
        out.write((int) (value >>> 48) & 0xFF);
        out.write((int) (value >>> 40) & 0xFF);
        out.write((int) (value >>> 32) & 0xFF);
        out.write((int) (value >>> 24) & 0xFF);
        out.write((int) (value >>> 16) & 0xFF);
        out.write((int) (value >>> 8) & 0xFF);
        out.write((int) value & 0xFF);
        return this;
    }

    public BigEndianWriter writeBytes(byte[] value) {
        Objects.requireNonNull(value, "value");
        out.write(value, 0, value.length);
        return this;
    }

    public int size() {
        return out.size();
    }

    public byte[] toByteArray() {
        return out.toByteArray();
    }
}
