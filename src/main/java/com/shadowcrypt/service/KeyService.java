package com.shadowcrypt.service;

import com.shadowcrypt.crypto.keystorage.PrivateKeyStore;
import com.shadowcrypt.crypto.keystorage.PublicKeyCodec;
import com.shadowcrypt.crypto.rsa.RsaKeyPairGenerator;

import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Objects;

/** Key workflows: generation, fingerprints, protected storage, and public exchange. */
public final class KeyService {

    public KeyPair generateKeyPair() {
        return RsaKeyPairGenerator.generate();
    }

    public String fingerprint(PublicKey publicKey) {
        return PublicKeyCodec.fingerprint(publicKey);
    }

    public String fingerprint(PrivateKey privateKey) {
        return PrivateKeyStore.fingerprint(privateKey);
    }

    public PublicKey derivePublicKey(PrivateKey privateKey) {
        return PrivateKeyStore.derivePublicKey(privateKey);
    }

    public void savePrivateKey(Path file, PrivateKey privateKey, char[] passphrase, boolean replaceExisting) {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(privateKey, "privateKey");
        PrivateKeyStore.save(file, privateKey, passphrase, replaceExisting);
    }

    public PrivateKey loadPrivateKey(Path file, char[] passphrase) {
        Objects.requireNonNull(file, "file");
        return PrivateKeyStore.load(file, passphrase);
    }

    public void exportPublicKey(Path file, PublicKey publicKey, boolean replaceExisting) {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(publicKey, "publicKey");
        PublicKeyCodec.write(file, publicKey, replaceExisting);
    }

    public PublicKey importPublicKey(Path file) {
        Objects.requireNonNull(file, "file");
        return PublicKeyCodec.read(file);
    }
}
