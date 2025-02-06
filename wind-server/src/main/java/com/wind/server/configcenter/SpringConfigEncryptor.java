package com.wind.server.configcenter;

import com.wind.common.WindConstants;
import com.wind.common.util.ServiceInfoUtils;
import com.wind.security.crypto.symmetric.AesTextEncryptor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wind.configcenter.core.ConfigRepository.PROPERTY_SOURCE_LOADER;

/**
 * 对特定格式的配置进行解密
 *
 * @author wuxp
 * @date 2025-02-02 10:51
 **/
@Getter
@AllArgsConstructor
@Slf4j
public final class SpringConfigEncryptor {

    public static final String SECRET_KEY = "WIND_SAE_KEY";

    private static SpringConfigEncryptor INSTANCE = getDefaultInstance();

    /**
     * 加密配置文件名称标记
     */
    static final String ENCRYPTION_CONFIG_FLAG = "-encryption";

    private final TextEncryptor encryptor;

    public CompositePropertySource encrypt(String name, String content) throws IOException {
        List<PropertySource<?>> sources = PROPERTY_SOURCE_LOADER.load(name, new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)));
        CompositePropertySource result = new CompositePropertySource(name);
        sources.forEach(source -> {
            if (isSensitiveConfig(source)) {
                MapPropertySource propertySource = new MapPropertySource(name, encryptValues(((MapPropertySource) source).getSource()));
                result.addFirstPropertySource(propertySource);
            } else {
                result.addFirstPropertySource(source);
            }
        });
        return result;
    }

    public String encryptTextConfig(String name, String content) throws IOException {
        CompositePropertySource sources = encrypt(name, content);
        StringBuilder result = new StringBuilder();
        sources.getPropertySources().forEach(source -> {
            if (source instanceof MapPropertySource) {
                ((MapPropertySource) source).getSource().forEach((k, v) -> result.append(k).append("=").append(v).append("\r\n"));
            }
        });
        return result.toString();
    }

    public PropertySource<?> decrypt(PropertySource<?> source) {
        if (isSensitiveConfig(source)) {
            Map<String, Object> result = decryptValues(((MapPropertySource) source).getSource());
            return new MapPropertySource(source.getName(), result);
        }
        return source;
    }

    private Map<String, Object> encryptValues(Map<String, Object> source) {
        Map<String, Object> result = new HashMap<>();
        source.forEach((key, val) -> result.put(key, encryptValue(key, val)));
        return Collections.unmodifiableMap(result);
    }

    private Object encryptValue(String key, Object val) {
        if (val instanceof OriginTrackedValue) {
            Object value = ((OriginTrackedValue) val).getValue();
            if (value instanceof String) {
                return encryptValueAsText(key, (String) value);
            }
        }
        if (val instanceof String) {
            return encryptValueAsText(key, (String) val);
        }
        return val;
    }

    private String encryptValueAsText(String key, String val) {
        try {
            return encryptor.encrypt(val);
        } catch (Exception exception) {
            log.debug("encrypt config = {} exception, message = {}", key, exception.getMessage(), exception);
        }
        return val;
    }

    private Map<String, Object> decryptValues(Map<String, Object> source) {
        Map<String, Object> result = new HashMap<>();
        source.forEach((key, val) -> result.put(key, decryptValue(key, val)));
        return Collections.unmodifiableMap(result);
    }

    private Object decryptValue(String key, Object val) {
        if (val instanceof OriginTrackedValue) {
            Object value = ((OriginTrackedValue) val).getValue();
            if (value instanceof String) {
                return decryptValueAsText(key, (String) value);
            }
        }
        if (val instanceof String) {
            return decryptValueAsText(key, (String) val);
        }
        return val;
    }

    private String decryptValueAsText(String key, String val) {
        try {
            return encryptor.decrypt(val);
        } catch (Exception exception) {
            log.debug("decrypt config = {} exception, message = {}", key, exception.getMessage(), exception);
        }
        return val;
    }

    private boolean isSensitiveConfig(PropertySource<?> source) {
        return source instanceof MapPropertySource && source.getName().contains(ENCRYPTION_CONFIG_FLAG);
    }

    public static void configEncryptor(TextEncryptor textEncryptor) {
        SpringConfigEncryptor.INSTANCE = new SpringConfigEncryptor(textEncryptor);
    }

    public static SpringConfigEncryptor getInstance() {
        return INSTANCE;
    }

    private static SpringConfigEncryptor getDefaultInstance() {
        String secret = ServiceInfoUtils.getSystemProperty(SECRET_KEY);
        if (!StringUtils.hasText(secret)) {
            secret = RandomStringUtils.random(64);
        }
        return new SpringConfigEncryptor(new AesTextEncryptor(secret, WindConstants.WIND));
    }
}
