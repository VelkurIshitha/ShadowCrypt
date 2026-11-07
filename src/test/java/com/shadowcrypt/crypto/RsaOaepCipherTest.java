package com.shadowcrypt.crypto;

import com.shadowcrypt.crypto.aes.AesSessionKey;
import com.shadowcrypt.crypto.rsa.RsaOaepCipher;
import com.shadowcrypt.exception.AuthenticationFailedException;
import com.shadowcrypt.exception.MalformedPackageException;
import com.shadowcrypt.support.CryptoFixtures;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RsaOaepCipherTest {

    @Test
    void wrapsToExpectedLengthAndUnwraps() {
        KeyPair pair = CryptoFixtures.rsaKeyPair();
        SecretKey sessionKey = AesSessionKey.generate().key();

        byte[] wrapped = RsaOaepCipher.wrap(pair.getPublic(), sessionKey);

        assertEquals(384, wrapped.length);
        SecretKey unwrapped = RsaOaepCipher.unwrap(pair.getPrivate(), wrapped);
        assertArrayEquals(sessionKey.getEncoded(), unwrapped.getEncoded());
    }

    @Test
    void rejectsWrongPrivateKey() {
        KeyPair pair = CryptoFixtures.rsaKeyPair();
        KeyPair other = CryptoFixtures.otherRsaKeyPair();
        SecretKey sessionKey = AesSessionKey.generate().key();
        byte[] wrapped = RsaOaepCipher.wrap(pair.getPublic(), sessionKey);

        assertThrows(AuthenticationFailedException.class,
                () -> RsaOaepCipher.unwrap(other.getPrivate(), wrapped));
    }

    @Test
    void rejectsWrappedKeyOfWrongLength() {
        KeyPair pair = CryptoFixtures.rsaKeyPair();

        assertThrows(MalformedPackageException.class,
                () -> RsaOaepCipher.unwrap(pair.getPrivate(), new byte[383]));
    }
}
