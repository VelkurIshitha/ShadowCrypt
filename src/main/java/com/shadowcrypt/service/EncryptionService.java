package com.shadowcrypt.service;

import com.shadowcrypt.config.AppConstants;
import com.shadowcrypt.config.CryptoConstants;
import com.shadowcrypt.crypto.aes.AesGcmCipher;
import com.shadowcrypt.crypto.aes.AesSessionKey;
import com.shadowcrypt.crypto.rsa.RsaOaepCipher;
import com.shadowcrypt.exception.CapacityExceededException;
import com.shadowcrypt.exception.CryptoOperationException;
import com.shadowcrypt.exception.FileSafetyException;
import com.shadowcrypt.files.importer.FileImporter;
import com.shadowcrypt.format.envelope.Envelope;
import com.shadowcrypt.format.envelope.EnvelopeSerializer;
import com.shadowcrypt.format.payload.InnerPayload;
import com.shadowcrypt.format.payload.PayloadSerializer;
import com.shadowcrypt.image.models.CarrierImage;
import com.shadowcrypt.image.normalization.CarrierNormalizer;
import com.shadowcrypt.image.processing.PngWriter;
import com.shadowcrypt.model.EncryptionRequest;
import com.shadowcrypt.model.EncryptionResult;
import com.shadowcrypt.model.MessagePayload;
import com.shadowcrypt.model.PayloadType;
import com.shadowcrypt.steganography.capacity.CapacityCalculator;
import com.shadowcrypt.steganography.encoding.LsbEncoder;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * End-to-end encryption: payload -> inner DATA -> AES-256-GCM with RSA-wrapped key
 * -> SHDW envelope -> LSB embedding -> PNG export. All measurements come from the
 * real operation.
 */
public final class EncryptionService {

    private static final String TEXT_MIME_TYPE = "text/plain; charset=utf-8";

    public EncryptionResult encrypt(EncryptionRequest request) {
        return encrypt(request, ProgressListener.NONE);
    }

    public EncryptionResult encrypt(EncryptionRequest request, ProgressListener progressListener) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(progressListener, "progressListener");

        progressListener.report(0.05, "Loading the carrier image");
        CarrierImage carrier = CarrierNormalizer.load(request.carrierImage());
        long capacity = CapacityCalculator.capacityBytes(carrier.width(), carrier.height());

        progressListener.report(0.2, "Preparing the payload");
        MessagePayload payload = resolvePayload(request, capacity);
        byte[] payloadData = payload.data();
        InnerPayload innerPayload = new InnerPayload(
                payload.type(), payload.fileName(), payload.mimeType(), payloadData);

        byte[] innerBytes = PayloadSerializer.serialize(innerPayload);
        progressListener.report(0.4, "Wrapping the AES session key");
        AesSessionKey sessionKey = AesSessionKey.generate();
        byte[] wrappedKey = RsaOaepCipher.wrap(request.recipientPublicKey(), sessionKey.key());
        byte[] nonce = sessionKey.nonce();

        int ciphertextLength = innerBytes.length + CryptoConstants.GCM_TAG_BYTES;
        byte[] aad = EnvelopeSerializer.aadBytes(0, wrappedKey, nonce, ciphertextLength);
        progressListener.report(0.55, "Encrypting the payload");
        byte[] ciphertext = AesGcmCipher.encrypt(sessionKey.key(), nonce, innerBytes, aad);
        if (ciphertext.length != ciphertextLength) {
            throw new CryptoOperationException("Unexpected AES-GCM ciphertext length");
        }

        Envelope envelope = new Envelope(0, wrappedKey, nonce, ciphertext);
        byte[] packageBytes = EnvelopeSerializer.serialize(envelope);
        CapacityCalculator.requireCapacity(packageBytes.length, carrier.width(), carrier.height());

        progressListener.report(0.7, "Embedding the package in the PNG");
        long embeddingStart = System.nanoTime();
        BufferedImage stegoImage = LsbEncoder.embed(carrier.image(), packageBytes);
        PngWriter.write(stegoImage, request.outputFile(), request.replaceExisting());
        long embeddingNanos = System.nanoTime() - embeddingStart;

        progressListener.report(1.0, "Done");
        return new EncryptionResult(
                request.outputFile(),
                payload.type(),
                payloadData.length,
                packageBytes.length,
                capacity,
                carrier.width(),
                carrier.height(),
                fileSize(request.carrierImage()),
                fileSize(request.outputFile()),
                embeddingNanos,
                carrier.image(),
                stegoImage);
    }

    private static MessagePayload resolvePayload(EncryptionRequest request, long capacity) {
        if (request.type() == PayloadType.TEXT) {
            byte[] data = request.text().getBytes(StandardCharsets.UTF_8);
            if (data.length > capacity) {
                throw new CapacityExceededException(data.length, capacity);
            }
            return new MessagePayload(PayloadType.TEXT, "", TEXT_MIME_TYPE, data);
        }

        Path source = request.sourceFile();
        long sourceSize = fileSize(source);
        if (sourceSize > capacity) {
            throw new CapacityExceededException(sourceSize, capacity);
        }
        FileImporter.ImportedFile imported = FileImporter.read(source, AppConstants.MAX_INPUT_FILE_BYTES);
        return new MessagePayload(PayloadType.FILE, imported.fileName(), imported.mimeType(), imported.data());
    }

    private static long fileSize(Path file) {
        try {
            return Files.size(file);
        } catch (IOException e) {
            throw new FileSafetyException("Unable to read file size: " + file, e);
        }
    }
}
