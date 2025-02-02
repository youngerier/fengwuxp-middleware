package com.wind.server.configcenter;

import com.wind.common.WindConstants;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static com.wind.configcenter.core.ConfigRepository.PROPERTY_SOURCE_LOADER;

/**
 * @author wuxp
 * @date 2025-02-02 11:32
 **/
class SpringConfigEncryptorTests {

    static {
        System.setProperty(SpringConfigEncryptor.SECRET_KEY, RandomStringUtils.randomAlphabetic(32));
    }

    private final SpringConfigEncryptor encryptor = SpringConfigEncryptor.getInstance();

    @Test
    void testEncrypt() throws Exception {
        String name = "exampley-mysql-encryption.properties";
        String config = encryptor.encryptTextConfig(name, "spring.datasource.username=test\n" +
                "spring.datasource.password=k1n231,.,fo123\n" +
                "spring.datasource.url=jdbc:mysql://example.com:3306/test_db");
        Optional<? extends PropertySource<?>> optional = PROPERTY_SOURCE_LOADER.load(name,
                        new ByteArrayResource(config.getBytes(StandardCharsets.UTF_8)))
                .stream()
                .map(encryptor::decrypt)
                .findFirst();
        Assertions.assertTrue(optional.isPresent());
        PropertySource<?> source = optional.get();
        Assertions.assertEquals("test", source.getProperty("spring.datasource.username"));
        Assertions.assertEquals("k1n231,.,fo123", source.getProperty("spring.datasource.password"));
        Assertions.assertEquals("jdbc:mysql://example.com:3306/test_db", source.getProperty("spring.datasource.url"));
    }
}
