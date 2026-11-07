package com.shadowcrypt.steganography;

import com.shadowcrypt.exception.CapacityExceededException;
import com.shadowcrypt.steganography.capacity.CapacityCalculator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CapacityCalculatorTest {

    @Test
    void computesTheoreticalCapacity() {
        assertEquals(3_750, CapacityCalculator.capacityBytes(100, 100));
        assertEquals(9, CapacityCalculator.capacityBytes(3, 8));
        assertEquals(0, CapacityCalculator.capacityBytes(1, 1));
    }

    @Test
    void rejectsNonPositiveDimensions() {
        assertThrows(IllegalArgumentException.class, () -> CapacityCalculator.capacityBytes(0, 10));
        assertThrows(IllegalArgumentException.class, () -> CapacityCalculator.capacityBytes(10, -1));
    }

    @Test
    void acceptsPayloadAtExactCapacity() {
        CapacityCalculator.requireCapacity(3_750, 100, 100);
    }

    @Test
    void rejectsPayloadOneByteOverCapacity() {
        CapacityExceededException exception = assertThrows(CapacityExceededException.class,
                () -> CapacityCalculator.requireCapacity(3_751, 100, 100));

        assertEquals(3_751, exception.requiredBytes());
        assertEquals(3_750, exception.capacityBytes());
    }
}
