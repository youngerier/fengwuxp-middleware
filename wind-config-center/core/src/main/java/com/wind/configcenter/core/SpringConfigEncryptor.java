package com.wind.configcenter.core;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.ImmutableSet;
import com.wind.common.WindConstants;
import com.wind.common.annotations.VisibleForTesting;
import com.wind.common.exception.AssertUtils;
import com.wind.common.util.ServiceInfoUtils;
import com.wind.core.WindCredentialsProvider;
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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import static com.wind.configcenter.core.ConfigRepository.PROPERTY_SOURCE_LOADER;
import static org.springframework.core.env.StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME;

/**
 * 对特定格式的配置进行解密或替换
 *
 * @author wuxp
 * @date 2025-02-02 10:51
 **/
@Getter
@Slf4j
@AllArgsConstructor
public final class SpringConfigEncryptor {

    private static final Set<String> REQUIRES_DECRYPT_NAMES = ImmutableSet.of(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);

    /**
     * 需要交换的凭据
     * 对于纯文本，配置格式: $$CRE_{凭据名称}，例如：$$CRE_example
     * 对于 key value 格式凭据，支持如下语法: $$CRE_{凭据名称}.{凭据配置 key}，例如：$$CRE_example.username
     */
    @VisibleForTesting
    static final String CREDENTIALS_PREFIX = "$$CRE_";

    /**
     * 需要解密的配置
     * 支持如下语法: $ENC_{配置内容密文}，例如: $$ENC_x3k24k234
     */
    @VisibleForTesting
    static final String ENCRYPT_PREFIX = "$$ENC_";

    public static final String SECRET_KEY = "WIND_SAE_KEY";

    private static final SpringConfigEncryptor INSTANCE = new SpringConfigEncryptor(getDefaultEncryptor(), getCredentialsProvider());

    private final TextEncryptor encryptor;

    private final WindCredentialsProvider credentialsProvider;

    public CompositePropertySource encrypt(String name, String content) throws IOException {
        List<PropertySource<?>> sources = PROPERTY_SOURCE_LOADER.load(name, new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)));
        CompositePropertySource result = new CompositePropertySource(name);
        sources.forEach(source -> {
            MapPropertySource propertySource = new MapPropertySource(name, encryptValues(((MapPropertySource) source).getSource()));
            result.addFirstPropertySource(propertySource);
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
        if (requiresDecrypt(source)) {
            Map<String, Object> keyValues = ((MapPropertySource) source).getSource();
            Map<String, Object> credentialsValues = new HashMap<>();
            Map<String, Object> decryptValues = new HashMap<>();

            keyValues.forEach((key, value) -> {
                String textValue = asText(value);
                if (textValue.startsWith(CREDENTIALS_PREFIX)) {
                    credentialsValues.put(key, value);
                } else if (textValue.startsWith(ENCRYPT_PREFIX)) {
                    decryptValues.put(key, value);
                }
            });
            if (credentialsValues.isEmpty() && decryptValues.isEmpty()) {
                // 没有需要处理的
                return source;
            }

            Map<String, Object> result = new HashMap<>(keyValues);
            result.putAll(getCredentials(credentialsValues));
            result.putAll(decryptValues(decryptValues));
            return new MapPropertySource(source.getName(), Collections.unmodifiableMap(result));
        }
        return source;
    }

    private Map<String, Object> getCredentials(Map<String, Object> source) {
        Map<String, Object> result = new HashMap<>();
        Set<String> credentialsNames = new HashSet<>();
        // 获取所有凭据的名称
        source.forEach((key, val) -> {
            String expression = asText(val).substring(CREDENTIALS_PREFIX.length());
            String[] parts = expression.split("\\.", 2);
            credentialsNames.add(parts[0]);
        });

        // 加载所有的凭据
        Map<String, Object> credentialsValues = new HashMap<>();
        credentialsNames.forEach(name -> {
            String text = credentialsProvider.getCredentials(name);
            if (text.startsWith(WindConstants.DELIM_START) && text.endsWith(WindConstants.DELIM_END)) {
                // json
                credentialsValues.put(name, JSON.parseObject(text));
            } else {
                credentialsValues.put(name, text);
            }
        });

        // 替换凭据
        source.forEach((key, val) -> {
            String expression = asText(val).substring(CREDENTIALS_PREFIX.length());
            String[] parts = expression.split("\\.", 2);
            Object value = credentialsValues.get(parts[0]);
            AssertUtils.notNull(value, () -> "not found credentials name = " + parts[0] + ", config key = " + key);
            if (value instanceof String) {
                result.put(key, value);
            } else {
                AssertUtils.isTrue(parts.length == 2, () -> "invalid credentials config, key  = " + key + " , value = " + value);
                JSONObject v = (JSONObject) value;
                result.put(key, v.get(parts[1]));
            }
        });
        return result;
    }

    private Map<String, Object> encryptValues(Map<String, Object> source) {
        Map<String, Object> result = new HashMap<>();
        source.forEach((key, val) -> {
            try {
                result.put(key, encryptor.encrypt(asText(val)));
            } catch (Exception exception) {
                log.debug("encrypt config = {} exception, message = {}", key, exception.getMessage(), exception);
                result.put(key, val);
            }
        });
        return result;
    }

    private String asText(Object val) {
        if (val instanceof OriginTrackedValue) {
            Object value = ((OriginTrackedValue) val).getValue();
            if (value instanceof String) {
                return (String) value;
            }
        }
        if (val instanceof String) {
            return (String) val;
        }
        return String.valueOf(val);
    }

    private Map<String, Object> decryptValues(Map<String, Object> source) {
        Map<String, Object> result = new HashMap<>();
        source.forEach((key, val) -> {
            try {
                // 删除掉前缀后解密
                result.put(key, encryptor.decrypt(asText(val).substring(ENCRYPT_PREFIX.length())));
            } catch (Exception exception) {
                log.debug("decrypt config = {} exception, message = {}", key, exception.getMessage(), exception);
                result.put(key, val);
            }
        });
        return Collections.unmodifiableMap(result);
    }

    private boolean requiresDecrypt(PropertySource<?> source) {
        String name = source.getName();
        if (REQUIRES_DECRYPT_NAMES.contains(name)) {
            return true;
        }
        return source instanceof MapPropertySource;
    }

    public static SpringConfigEncryptor getInstance() {
        return INSTANCE;
    }

    private static TextEncryptor getDefaultEncryptor() {
        String secret = ServiceInfoUtils.getSystemProperty(SECRET_KEY);
        if (!StringUtils.hasText(secret)) {
            secret = RandomStringUtils.random(64);
        }
        return new AesTextEncryptor(secret, WindConstants.WIND);
    }

    @NotNull
    private static WindCredentialsProvider getCredentialsProvider() {
        ServiceLoader<WindCredentialsProvider> services = ServiceLoader.load(WindCredentialsProvider.class);
        for (WindCredentialsProvider e : services) {
            return e;
        }
        return SpringConfigEncryptor::getCredentials;
    }

    private static String getCredentials(@NotBlank String credentialsName) {
        throw new UnsupportedOperationException("un supported");
    }
}
