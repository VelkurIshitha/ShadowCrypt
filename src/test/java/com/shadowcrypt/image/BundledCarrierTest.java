package com.shadowcrypt.image;

import com.shadowcrypt.config.AppConstants;
import com.shadowcrypt.image.models.CarrierImage;
import com.shadowcrypt.image.normalization.CarrierNormalizer;
import com.shadowcrypt.steganography.capacity.CapacityCalculator;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BundledCarrierTest {

    @Test
    void everyBundledCarrierLoadsAsOpaqueRgbPng() {
        for (String resource : AppConstants.BUNDLED_CARRIER_RESOURCES) {
            CarrierImage carrier = CarrierNormalizer.loadResource(resource);

            assertTrue(carrier.width() > 0, resource + " width");
            assertTrue(carrier.height() > 0, resource + " height");
            assertEquals(BufferedImage.TYPE_INT_RGB, carrier.image().getType(), resource);
            assertTrue(CapacityCalculator.capacityBytes(carrier.width(), carrier.height()) > 1_000,
                    resource + " capacity");
        }
    }
}
