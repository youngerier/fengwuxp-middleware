package com.wind.security.crypto;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

/**
 * @author wuxp
 * @date 2025-02-02 20:18
 **/
class RasTextEncryptorTests {

    @Test
    void testOfPrivateEncrypt() {
        KeyPair keyPair = genKeyPir();
        RasTextEncryptor encryptor = RasTextEncryptor.ofPrivateEncrypt(keyPair.getPrivate(), keyPair.getPublic());
        String text = RandomStringUtils.secure().nextAlphabetic(32);
        String encrypt = encryptor.encrypt(text);
        Assertions.assertEquals(text, encryptor.decrypt(encrypt));
        Assertions.assertEquals(text, RasTextEncryptor.ofPrivateEncrypt(null, keyPair.getPublic()).decrypt(encrypt));
    }

    @Test
    void testOfPublic() {
        KeyPair keyPair = genKeyPir();
        RasTextEncryptor encryptor = RasTextEncryptor.ofPublicEncrypt(keyPair.getPublic(), keyPair.getPrivate());
        String text = RandomStringUtils.secure().nextAlphabetic(32);
        String encrypt = encryptor.encrypt(text);
        Assertions.assertEquals(text, encryptor.decrypt(encrypt));
        Assertions.assertEquals(text, RasTextEncryptor.ofPublicEncrypt(null, keyPair.getPrivate()).decrypt(encrypt));
    }

    private KeyPair genKeyPir() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
