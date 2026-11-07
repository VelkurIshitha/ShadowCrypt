package com.shadowcrypt.service;

import com.shadowcrypt.config.PackageConstants;
import com.shadowcrypt.crypto.aes.AesGcmCipher;
import com.shadowcrypt.crypto.hashing.Sha256;
import com.shadowcrypt.crypto.keystorage.PrivateKeyStore;
import com.shadowcrypt.crypto.rsa.RsaOaepCipher;
import com.shadowcrypt.exception.MalformedPackageException;
import com.shadowcrypt.files.exporter.SafeFileExporter;
import com.shadowcrypt.files.validation.FileNameSanitizer;
import com.shadowcrypt.format.envelope.Envelope;
import com.shadowcrypt.format.envelope.EnvelopeParser;
import com.shadowcrypt.format.payload.InnerPayload;
import com.shadowcrypt.format.payload.PayloadParser;
import com.shadowcrypt.image.models.CarrierImage;
import com.shadowcrypt.image.normalization.CarrierNormalizer;
import com.shadowcrypt.model.DecryptionRequest;
import com.shadowcrypt.model.DecryptionResult;
import com.shadowcrypt.model.PayloadType;
import com.shadowcrypt.steganography.capacity.CapacityCalculator;
import com.shadowcrypt.steganography.extraction.LsbDecoder;
import com.shadowcrypt.util.SecureWipe;

import javax.crypto.SecretKey;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.util.Objects;

/**
 * End-to-end decryption: read the bootstrap header, validate before allocating,
 * extract the package, unwrap the AES key, authenticate and decrypt, then recover
 * text or safely export a file.
 */
public final class DecryptionService {

    public DecryptionResult decrypt(DecryptionRequest request) {
        return decrypt(request, ProgressListener.NONE);
    }

    public DecryptionResult decrypt(DecryptionRequest request, ProgressListener progressListener) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(progressListener, "progressListener");

        long extractionStart = System.nanoTime();

        progressListener.report(0.1, "Reading the stego image");
        CarrierImage stego = CarrierNormalizer.load(request.stegoImage());
        long capacity = CapacityCalculator.capacityBytes(stego.width(), stego.height());
        if (capacity < PackageConstants.BOOTSTRAP_HEADER_BYTES) {
            throw new MalformedPackageException("The image is too small to contain a ShadowCrypt package");
        }

        progressListener.report(0.25, "Validating the package header");
        byte[] bootstrapBytes = LsbDecoder.extract(stego.image(), PackageConstants.BOOTSTRAP_HEADER_BYTES);
        EnvelopeParser.Bootstrap bootstrap = EnvelopeParser.parseBootstrap(bootstrapBytes);
        if (bootstrap.totalPackageLength() > capacity) {
            throw new MalformedPackageException("The declared package length exceeds the carrier capacity");
        }

        progressListener.report(0.4, "Extracting the package");
        byte[] packageBytes = LsbDecoder.extract(stego.image(), bootstrap.totalPackageLength());
        Envelope envelope = EnvelopeParser.parse(packageBytes, capacity);
        byte[] aad = EnvelopeParser.aadBytes(packageBytes);

        long extractionNanos = System.nanoTime() - extractionStart;
        long decryptionStart = System.nanoTime();

        progressListener.report(0.55, "Unlocking the private key");
        PrivateKey privateKey;
        char[] passphrase = request.passphrase();
        try {
            privateKey = PrivateKeyStore.load(request.privateKeyFile(), passphrase);
        } finally {
            SecureWipe.wipe(passphrase);
        }

        progressListener.report(0.7, "Unwrapping the session key");
        SecretKey sessionKey = RsaOaepCipher.unwrap(privateKey, envelope.wrappedKey());
        progressListener.report(0.8, "Decrypting and authenticating the payload");
        byte[] plaintext = AesGcmCipher.decrypt(
                sessionKey, envelope.nonce(), envelope.ciphertext(), aad);
        InnerPayload payload = PayloadParser.parse(plaintext);
        byte[] payloadData = payload.data();
        String sha256 = Sha256.hex(payloadData);

        progressListener.report(0.92, "Recovering the payload");
        DecryptionResult result;
        if (payload.type() == PayloadType.TEXT) {
            result = new DecryptionResult(
                    PayloadType.TEXT,
                    decodeText(payloadData),
                    null,
                    "",
                    payload.mimeType(),
                    sha256,
                    packageBytes.length,
                    extractionNanos,
                    System.nanoTime() - decryptionStart);
        } else {
            String safeName = FileNameSanitizer.sanitize(payload.fileName());
            Path recoveredFile = SafeFileExporter.write(
                    request.outputDirectory(), safeName, payloadData, request.replaceExisting());
            result = new DecryptionResult(
                    PayloadType.FILE,
                    null,
                    recoveredFile,
                    recoveredFile.getFileName().toString(),
                    payload.mimeType(),
                    sha256,
                    packageBytes.length,
                    extractionNanos,
                    System.nanoTime() - decryptionStart);
        }
        progressListener.report(1.0, "Done");
        return result;
    }

    private static String decodeText(byte[] data) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(data))
                    .toString();
        } catch (CharacterCodingException e) {
            throw new MalformedPackageException("The recovered text is not valid UTF-8", e);
        }
    }
}
