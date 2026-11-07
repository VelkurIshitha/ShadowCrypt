package com.shadowcrypt.format.envelope;

import com.shadowcrypt.config.CryptoConstants;
import com.shadowcrypt.config.PackageConstants;
import com.shadowcrypt.exception.MalformedPackageException;
import com.shadowcrypt.format.serialization.BigEndianReader;

import java.util.Arrays;
import java.util.Objects;

/**
 * Strict envelope parser. Fixed header fields are validated before any buffer is
 * sized from untrusted lengths, and the declared total must match reality exactly.
 */
public final class EnvelopeParser {

    private EnvelopeParser() {
    }

    /** Validated fields from the fixed 17-byte bootstrap header. */
    public record Bootstrap(int flags, int totalPackageLength, int wrappedKeyLength,
                            int nonceLength, int ciphertextLength) {
    }

    public static Bootstrap parseBootstrap(byte[] headerBytes) {
        Objects.requireNonNull(headerBytes, "headerBytes");
        if (headerBytes.length < PackageConstants.BOOTSTRAP_HEADER_BYTES) {
            throw new MalformedPackageException("The package header is truncated: "
                    + headerBytes.length + " of " + PackageConstants.BOOTSTRAP_HEADER_BYTES + " bytes");
        }

        BigEndianReader reader = new BigEndianReader(
                headerBytes, 0, PackageConstants.BOOTSTRAP_HEADER_BYTES);

        byte[] magic = reader.readBytes(PackageConstants.MAGIC_BYTES);
        if (!Arrays.equals(magic, PackageConstants.MAGIC)) {
            throw new MalformedPackageException("Not a ShadowCrypt package (bad magic number)");
        }

        int version = reader.readUnsignedByte();
        if (version != PackageConstants.VERSION) {
            throw new MalformedPackageException("Unsupported package version: " + version);
        }

        int flags = reader.readUnsignedByte();
        if ((flags & ~PackageConstants.KNOWN_FLAGS_MASK) != 0) {
            throw new MalformedPackageException("Unsupported package flags: 0x"
                    + Integer.toHexString(flags));
        }

        int totalPackageLength = reader.readInt();
        int wrappedKeyLength = reader.readUnsignedShort();
        int nonceLength = reader.readUnsignedByte();
        int ciphertextLength = reader.readInt();

        if (wrappedKeyLength != CryptoConstants.RSA_WRAPPED_KEY_BYTES) {
            throw new MalformedPackageException("Invalid wrapped-key length: " + wrappedKeyLength
                    + " (expected " + CryptoConstants.RSA_WRAPPED_KEY_BYTES + ")");
        }
        if (nonceLength != CryptoConstants.GCM_NONCE_BYTES) {
            throw new MalformedPackageException("Invalid nonce length: " + nonceLength
                    + " (expected " + CryptoConstants.GCM_NONCE_BYTES + ")");
        }
        if (ciphertextLength < CryptoConstants.GCM_TAG_BYTES) {
            throw new MalformedPackageException("Invalid ciphertext length: " + ciphertextLength
                    + " (must include a " + CryptoConstants.GCM_TAG_BYTES + "-byte tag)");
        }

        long expectedTotal = (long) PackageConstants.BOOTSTRAP_HEADER_BYTES
                + wrappedKeyLength + nonceLength + ciphertextLength;
        if (expectedTotal > Integer.MAX_VALUE) {
            throw new MalformedPackageException("The declared package is too large");
        }
        if (totalPackageLength != expectedTotal) {
            throw new MalformedPackageException("The declared package length does not match its fields");
        }

        return new Bootstrap(flags, totalPackageLength, wrappedKeyLength, nonceLength, ciphertextLength);
    }

    public static Envelope parse(byte[] packageBytes) {
        return parse(packageBytes, Long.MAX_VALUE);
    }

    /**
     * Parses a complete package and verifies it fits the carrier capacity.
     *
     * @param packageBytes         exactly the extracted package bytes
     * @param carrierCapacityBytes theoretical capacity of the carrier image
     */
    public static Envelope parse(byte[] packageBytes, long carrierCapacityBytes) {
        Objects.requireNonNull(packageBytes, "packageBytes");
        Bootstrap bootstrap = parseBootstrap(packageBytes);

        if (bootstrap.totalPackageLength() > carrierCapacityBytes) {
            throw new MalformedPackageException("The declared package length exceeds the carrier capacity");
        }
        if (packageBytes.length != bootstrap.totalPackageLength()) {
            throw new MalformedPackageException("The package length does not match its declared length ("
                    + packageBytes.length + " vs " + bootstrap.totalPackageLength() + " bytes)");
        }

        int offset = PackageConstants.BOOTSTRAP_HEADER_BYTES;
        byte[] wrappedKey = Arrays.copyOfRange(packageBytes, offset, offset + bootstrap.wrappedKeyLength());
        offset += bootstrap.wrappedKeyLength();
        byte[] nonce = Arrays.copyOfRange(packageBytes, offset, offset + bootstrap.nonceLength());
        offset += bootstrap.nonceLength();
        byte[] ciphertext = Arrays.copyOfRange(packageBytes, offset, offset + bootstrap.ciphertextLength());

        return new Envelope(bootstrap.flags(), wrappedKey, nonce, ciphertext);
    }

    /** Returns the exact AAD prefix from the extracted package bytes. */
    public static byte[] aadBytes(byte[] packageBytes) {
        Objects.requireNonNull(packageBytes, "packageBytes");
        if (packageBytes.length < EnvelopeSerializer.AAD_BYTES) {
            throw new MalformedPackageException("The package is too short to contain its authenticated header");
        }
        return Arrays.copyOfRange(packageBytes, 0, EnvelopeSerializer.AAD_BYTES);
    }
}
