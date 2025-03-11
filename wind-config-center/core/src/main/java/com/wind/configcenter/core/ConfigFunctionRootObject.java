package com.wind.configcenter.core;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wind.common.WindConstants;
import com.wind.common.annotations.VisibleForTesting;
import com.wind.common.util.ServiceInfoUtils;
import com.wind.core.WindCredentialsProvider;
import com.wind.security.crypto.symmetric.AesTextEncryptor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ServiceLoader;

/**
 * 用于配置执行的 spring expression root object
 *
 * @author wuxp
 * @date 2025-03-11 10:44
 **/
@Slf4j
@AllArgsConstructor
@Getter
final class ConfigFunctionRootObject {

    @VisibleForTesting
    static final String SECRET_KEY = "WIND_SAE_KEY";

    private final TextEncryptor encryptor;

    private final WindCredentialsProvider windCredentialsProvider;

    public ConfigFunctionRootObject() {
        this(getDefaultEncryptor(), getCredentialsProvider());
    }

    /**
     * 配置解密
     *
     * @param content 待解密内容
     * @return 解密结果
     */
    public String DEC(String content) {
        try {
            return encryptor.decrypt(content);
        } catch (Exception exception) {
            log.debug("decrypt config failure, content = {}", content, exception);
            return content;
        }
    }

    /**
     * 获取凭据的函数
     *
     * @param name 凭据名称
     * @return 凭据内容
     */
    public String CRE(String name) {
        return CRE(name, null);
    }

    /**
     * 获取凭据的函数
     *
     * @param name 凭据名称
     * @param key  凭据 key
     * @return 凭据内容
     */
    public String CRE(String name, @Nullable String key) {
        String credentials = windCredentialsProvider.getCredentials(name);
        if (StringUtils.hasText(key)) {
            JSONObject values = JSON.parseObject(credentials);
            return (String) values.get(key);
        }
        return credentials;
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
        return ConfigFunctionRootObject::getCredentials;
    }

    private static String getCredentials(@NotBlank String credentialsName) {
        throw new UnsupportedOperationException("un supported");
    }
}
