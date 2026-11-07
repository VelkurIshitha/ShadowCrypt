package com.shadowcrypt.exception;

/** The package does not fit in the carrier image's theoretical capacity. */
public class CapacityExceededException extends ShadowCryptException {

    private final long requiredBytes;
    private final long capacityBytes;

    public CapacityExceededException(long requiredBytes, long capacityBytes) {
        super("The package needs " + requiredBytes + " bytes but the carrier can hold only "
                + capacityBytes + " bytes");
        this.requiredBytes = requiredBytes;
        this.capacityBytes = capacityBytes;
    }

    public long requiredBytes() {
        return requiredBytes;
    }

    public long capacityBytes() {
        return capacityBytes;
    }
}
