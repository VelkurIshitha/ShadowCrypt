package com.shadowcrypt.integration;

import com.shadowcrypt.model.KeyPairInfo;
import com.shadowcrypt.service.KeyService;
import com.shadowcrypt.support.CryptoFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class KeyWorkflowTest {

    private static final char[] PASSPHRASE = "key-workflow-passphrase".toCharArray();

    @TempDir
    Path tempDirectory;

    private final KeyService keyService = new KeyService();

    @Test
    void generatesRsa3072KeyPair() {
        KeyPair pair = keyService.generateKeyPair();

        assertEquals(3072, ((RSAKey) pair.getPublic()).getModulus().bitLength());
        assertEquals(3072, ((RSAKey) pair.getPrivate()).getModulus().bitLength());
    }

    @Test
    void savesLoadsAndDerivesMatchingPublicKey() {
        KeyPair pair = CryptoFixtures.rsaKeyPair();
        Path privateKeyFile = tempDirectory.resolve("owner.scpriv");

        keyService.savePrivateKey(privateKeyFile, pair.getPrivate(), PASSPHRASE, false);
        PrivateKey loaded = keyService.loadPrivateKey(privateKeyFile, PASSPHRASE);
        PublicKey derivedPublic = keyService.derivePublicKey(loaded);

        assertArrayEquals(pair.getPrivate().getEncoded(), loaded.getEncoded());
        assertArrayEquals(pair.getPublic().getEncoded(), derivedPublic.getEncoded());
        assertEquals(keyService.fingerprint(pair.getPublic()), keyService.fingerprint(derivedPublic));
    }

    @Test
    void exportsAndImportsPublicKeyWithStableFingerprint() {
        PublicKey publicKey = CryptoFixtures.rsaKeyPair().getPublic();
        Path publicKeyFile = tempDirectory.resolve("recipient.scpub");

        keyService.exportPublicKey(publicKeyFile, publicKey, false);
        PublicKey imported = keyService.importPublicKey(publicKeyFile);

        assertArrayEquals(publicKey.getEncoded(), imported.getEncoded());
        assertEquals(keyService.fingerprint(publicKey), keyService.fingerprint(imported));

        KeyPairInfo info = new KeyPairInfo(imported, keyService.fingerprint(imported), null, publicKeyFile);
        assertEquals(publicKeyFile, info.publicKeyFile());
    }
}
