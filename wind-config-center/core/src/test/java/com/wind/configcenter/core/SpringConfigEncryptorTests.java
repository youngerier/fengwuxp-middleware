package com.wind.configcenter.core;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.ImmutableMap;
import com.wind.common.WindConstants;
import com.wind.security.crypto.symmetric.AesTextEncryptor;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.util.HashMap;
import java.util.Map;

import static com.wind.configcenter.core.SpringConfigEncryptor.CREDENTIALS_PREFIX;
import static com.wind.configcenter.core.SpringConfigEncryptor.ENCRYPT_PREFIX;

/**
 * @author wuxp
 * @date 2025-02-18 14:27
 **/
class SpringConfigEncryptorTests {

    private final TextEncryptor textEncryptor = new AesTextEncryptor(RandomStringUtils.randomAlphabetic(32), WindConstants.DEFAULT_TEXT);

    private final SpringConfigEncryptor encryptor = new SpringConfigEncryptor(textEncryptor, key -> {
        if (key.contains("text")) {
            return key.concat("text");
        }
        Map<String, String> config = ImmutableMap.of("userName", "a", "password", "abc",
                "name", "zhans", "accountPassword", "abc2", "rsaPublic", "1");
        return JSON.toJSONString(config);
    });

    @Test
    void testEncrypt() throws Exception {
        CompositePropertySource result = encryptor.encrypt("test.properties", "a=1\r\rp=2");
        Assertions.assertNotNull(result);
    }

    @Test
    void testDecrypt() throws Exception {

        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put("test.example", "a");
        keyValues.put("test.example.enc", ENCRYPT_PREFIX + encryptor.getEncryptor().encrypt("test"));
        keyValues.put("test.user.name", CREDENTIALS_PREFIX + "example.userName");
        keyValues.put("test.user.password", CREDENTIALS_PREFIX + "example.password");
        keyValues.put("test.rds.accountName", CREDENTIALS_PREFIX + "rds_dev.name");
        keyValues.put("test.rds.password", CREDENTIALS_PREFIX + "rds_dev.accountPassword");
        keyValues.put("test.rds.value", CREDENTIALS_PREFIX + "rds_dev.value");
        keyValues.put("test.jwt.key", CREDENTIALS_PREFIX + "jwt_text_key");
        keyValues.put("test.jwt.rasPublicKey", CREDENTIALS_PREFIX + "jwt_rsa_key.rsaPublic");
        keyValues.put("test.jwt.rasPrivateKey", CREDENTIALS_PREFIX + "jwt_rsa_key.rasPrivate");
        MapPropertySource source = new MapPropertySource("test", keyValues);
        PropertySource<?> result = encryptor.decrypt(source);
        Assertions.assertEquals("abc2", result.getProperty("test.rds.password"));
        Assertions.assertEquals("a", result.getProperty("test.user.name"));
        Assertions.assertEquals("zhans", result.getProperty("test.rds.accountName"));
        Assertions.assertNull(result.getProperty("jwt_rsa_key.rasPrivate"));
    }
}
