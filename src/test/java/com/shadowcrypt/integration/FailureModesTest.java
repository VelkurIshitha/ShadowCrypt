package com.shadowcrypt.integration;

import com.shadowcrypt.exception.AuthenticationFailedException;
import com.shadowcrypt.exception.CapacityExceededException;
import com.shadowcrypt.exception.KeyStorageException;
import com.shadowcrypt.exception.MalformedPackageException;
import com.shadowcrypt.image.normalization.CarrierNormalizer;
import com.shadowcrypt.image.processing.PngWriter;
import com.shadowcrypt.model.DecryptionRequest;
import com.shadowcrypt.model.DecryptionResult;
import com.shadowcrypt.model.EncryptionRequest;
import com.shadowcrypt.model.EncryptionResult;
import com.shadowcrypt.model.PayloadType;
import com.shadowcrypt.service.DecryptionService;
import com.shadowcrypt.service.EncryptionService;
import com.shadowcrypt.service.KeyService;
import com.shadowcrypt.support.CryptoFixtures;
import com.shadowcrypt.support.TestCarrierFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FailureModesTest {

    private static final char[] PASSPHRASE = "failure-mode-passphrase".toCharArray();
    private static final String TEXT = "this message will be tampered with";

    @TempDir
    Path tempDirectory;

    private final KeyService keyService = new KeyService();
    private final EncryptionService encryptionService = new EncryptionService();
    private final DecryptionService decryptionService = new DecryptionService();

    private KeyPair recipient;
    private Path carrierFile;
    private Path privateKeyFile;
    private Path outputDirectory;

    @BeforeEach
    void setUp() throws IOException {
        recipient = CryptoFixtures.rsaKeyPair();
        carrierFile = TestCarrierFactory.writePng(
                TestCarrierFactory.randomCarrier(200, 200, 31L), tempDirectory, "carrier.png");
        privateKeyFile = tempDirectory.resolve("recipient.scpriv");
        keyService.savePrivateKey(privateKeyFile, recipient.getPrivate(), PASSPHRASE, false);
        outputDirectory = tempDirectory.resolve("out");
        Files.createDirectories(outputDirectory);
    }

    @Test
    void rejectsIncorrectPrivateKey() {
        Path otherKeyFile = tempDirectory.resolve("other.scpriv");
        keyService.savePrivateKey(otherKeyFile, CryptoFixtures.otherRsaKeyPair().getPrivate(),
                PASSPHRASE, false);
        Path stego = encryptText(tempDirectory.resolve("wrong-key-stego.png"));

        assertThrows(AuthenticationFailedException.class,
                () -> decrypt(stego, otherKeyFile, PASSPHRASE));
    }

    @Test
    void rejectsIncorrectPassphrase() {
        Path stego = encryptText(tempDirectory.resolve("wrong-pass-stego.png"));

        assertThrows(KeyStorageException.class,
                () -> decrypt(stego, privateKeyFile, "definitely-wrong".toCharArray()));
    }

    @Test
    void rejectsTamperedCiphertext() {
        Path stego = encryptText(tempDirectory.resolve("tamper-source.png"));
        Path tampered = tamperPixel(stego, 1_200, "tampered-ciphertext.png");

        assertThrows(AuthenticationFailedException.class,
                () -> decrypt(tampered, privateKeyFile, PASSPHRASE));
    }

    @Test
    void rejectsInvalidMagicNumber() {
        Path stego = encryptText(tempDirectory.resolve("magic-source.png"));
        Path tampered = tamperPixel(stego, 0, "bad-magic.png");

        assertThrows(MalformedPackageException.class,
                () -> decrypt(tampered, privateKeyFile, PASSPHRASE));
    }

    @Test
    void rejectsInvalidLengthField() {
        Path stego = encryptText(tempDirectory.resolve("length-source.png"));
        Path tampered = tamperPixel(stego, 16, "bad-length.png");

        assertThrows(MalformedPackageException.class,
                () -> decrypt(tampered, privateKeyFile, PASSPHRASE));
    }

    @Test
    void rejectsCarrierThatIsTooSmallForThePackage() throws IOException {
        Path tinyCarrier = TestCarrierFactory.writePng(
                TestCarrierFactory.randomCarrier(8, 8, 32L), tempDirectory, "tiny.png");
        Path source = tempDirectory.resolve("one-byte.bin");
        Files.write(source, new byte[]{1});

        assertThrows(CapacityExceededException.class, () -> encryptionService.encrypt(
                new EncryptionRequest(PayloadType.FILE, null, source, tinyCarrier,
                        recipient.getPublic(), tempDirectory.resolve("tiny-stego.png"), false)));
    }

    @Test
    void acceptsPackageExactlyAtCapacityAndRejectsOneByteOver() {
        EncryptionResult sizing = encryptionService.encrypt(new EncryptionRequest(
                PayloadType.TEXT, TEXT, null, carrierFile, recipient.getPublic(),
                tempDirectory.resolve("sizing-stego.png"), false));
        int packageBytes = (int) sizing.packageBytes();

        BufferedImage exactCarrier = TestCarrierFactory.carrierWithCapacityExactly(packageBytes);
        Path exactCarrierFile = writePng(exactCarrier, "exact-carrier.png");
        Path exactStego = tempDirectory.resolve("exact-stego.png");

        EncryptionResult exact = encryptionService.encrypt(new EncryptionRequest(
                PayloadType.TEXT, TEXT, null, exactCarrierFile, recipient.getPublic(), exactStego, false));
        assertEquals(packageBytes, exact.packageBytes());
        DecryptionResult decrypted = decrypt(exactStego, privateKeyFile, PASSPHRASE);
        assertEquals(TEXT, decrypted.text());

        Path overCarrierFile = writePng(
                TestCarrierFactory.carrierWithCapacityExactly(packageBytes - 1), "over-carrier.png");

        assertThrows(CapacityExceededException.class, () -> encryptionService.encrypt(
                new EncryptionRequest(PayloadType.TEXT, TEXT, null, overCarrierFile,
                        recipient.getPublic(), tempDirectory.resolve("over-stego.png"), false)));
    }

    private Path encryptText(Path stegoFile) {
        return encryptionService.encrypt(new EncryptionRequest(
                PayloadType.TEXT, TEXT, null, carrierFile, recipient.getPublic(), stegoFile, false))
                .stegoFile();
    }

    private DecryptionResult decrypt(Path stego, Path keyFile, char[] passphrase) {
        return decryptionService.decrypt(
                new DecryptionRequest(stego, keyFile, passphrase, outputDirectory, false));
    }

    private Path tamperPixel(Path stegoFile, int pixelIndex, String outputName) {
        BufferedImage image = CarrierNormalizer.load(stegoFile).image();
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
        pixels[pixelIndex] ^= 0x00010000;
        image.setRGB(0, 0, width, height, pixels, 0, width);

        Path output = tempDirectory.resolve(outputName);
        PngWriter.write(image, output, false);
        return output;
    }

    private Path writePng(BufferedImage image, String name) {
        try {
            return TestCarrierFactory.writePng(image, tempDirectory, name);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
