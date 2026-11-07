package com.shadowcrypt.service;

import com.shadowcrypt.analysis.AnalysisCalculator;
import com.shadowcrypt.model.AnalysisReport;
import com.shadowcrypt.model.EncryptionResult;

import java.util.Objects;

/** Turns a real encryption result into the analysis report shown in the UI. */
public final class AnalysisService {

    public AnalysisReport analyze(EncryptionResult encryptionResult) {
        Objects.requireNonNull(encryptionResult, "encryptionResult");
        return AnalysisCalculator.calculate(
                encryptionResult.normalizedCarrier(),
                encryptionResult.stegoImage(),
                encryptionResult.originalPngBytes(),
                encryptionResult.stegoPngBytes(),
                encryptionResult.payloadBytes(),
                encryptionResult.packageBytes(),
                encryptionResult.capacityBytes(),
                encryptionResult.embeddingNanos());
    }
}
