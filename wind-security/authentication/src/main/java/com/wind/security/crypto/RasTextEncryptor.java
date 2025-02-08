package com.wind.security.crypto;

import lombok.AllArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.util.Base64Utils;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * ras TextEncryptor
 * 支持公钥加密私钥解密 或 私钥加密公钥解密
 *
 * @author wuxp
 * @date 2025-02-02 19:56
 **/
@AllArgsConstructor
public final class RasTextEncryptor implements TextEncryptor {

    private static final String ALGORITHM = "RSA/ECB/PKCS1Padding";

    private final BytesEncryptor delegate;

    /**
     * 私钥加密，公钥解密
     *
     * @param publicKey  Rsa 公钥
     * @param privateKey rsa 私钥
     * @return RasTextEncryptor
     */
    public static RasTextEncryptor ofPrivateEncrypt(@Nullable PrivateKey privateKey, @Nullable PublicKey publicKey) {
        return new RasTextEncryptor(new RsaPrivateEncryptor(privateKey, publicKey));
    }

    /**
     * 公钥加密，私钥解密
     *
     * @param publicKey  Rsa 公钥
     * @param privateKey rsa 私钥
     * @return RasTextEncryptor
     */
    public static RasTextEncryptor ofPublicEncrypt(@Nullable PublicKey publicKey, @Nullable PrivateKey privateKey) {
        return new RasTextEncryptor(new RsaPublicEncryptor(publicKey, privateKey));
    }

    @Override
    public String encrypt(String text) {
        return Base64Utils.encodeToString(delegate.encrypt(text.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public String decrypt(String encryptedText) {
        return new String(delegate.decrypt(Base64Utils.decodeFromString(encryptedText)));
    }

    /**
     * 公钥加密，私钥解密
     */
    private static class RsaPublicEncryptor implements BytesEncryptor {

        private final PublicKey publicKey;

        private final PrivateKey privateKey;

        public RsaPublicEncryptor(PublicKey publicKey, PrivateKey privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        @Override
        public byte[] encrypt(byte[] byteArray) {
            try {
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                return cipher.doFinal(byteArray);
            } catch (Exception exception) {
                throw new IllegalArgumentException("encrypt exception!", exception);
            }
        }

        @Override
        public byte[] decrypt(byte[] encryptedByteArray) {
            try {
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                return cipher.doFinal(encryptedByteArray);
            } catch (Exception exception) {
                throw new IllegalArgumentException("decrypt exception!", exception);
            }
        }

    }

    /**
     * 私钥加密，公钥解密
     */
    private static class RsaPrivateEncryptor implements BytesEncryptor {

        private final PrivateKey privateKey;

        private final PublicKey publicKey;

        public RsaPrivateEncryptor(PrivateKey privateKey, PublicKey publicKey) {
            this.privateKey = privateKey;
            this.publicKey = publicKey;
        }

        @Override
        public byte[] encrypt(byte[] bytes) {
            try {
                // 使用私钥加密
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.ENCRYPT_MODE, privateKey);
                return cipher.doFinal(bytes);
            } catch (Exception exception) {
                throw new IllegalArgumentException("encrypt exception!", exception);
            }
        }

        @Override
        public byte[] decrypt(byte[] encryptedByteArray) {
            try {
                // 使用公钥解密
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.DECRYPT_MODE, publicKey);
                return cipher.doFinal(encryptedByteArray);
            } catch (Exception exception) {
                throw new IllegalArgumentException("decrypt exception!", exception);
            }
        }
    }
}
