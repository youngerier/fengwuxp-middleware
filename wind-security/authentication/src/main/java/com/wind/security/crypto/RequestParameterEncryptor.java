package com.wind.security.crypto;

import com.wind.common.annotations.VisibleForTesting;
import com.wind.common.exception.AssertUtils;
import com.wind.security.authentication.WindAuthenticationProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.util.StringUtils;

import java.security.KeyPair;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 接口请求参数加密器
 *
 * @author wuxp
 * @date 2023-11-02 20:24
 **/
@Slf4j
public final class RequestParameterEncryptor implements ApplicationListener<ApplicationStartedEvent> {

    /**
     * 加密配置
     */
    @VisibleForTesting
    static final AtomicReference<TextEncryptor> TEXT_ENCRYPTOR = new AtomicReference<>();

    /**
     * 加密请求内容
     *
     * @param content 加密内容
     * @return 加密结果
     */
    public static String encrypt(@NotNull String content) {
        TextEncryptor encryptor = getEncryptor();
        return StringUtils.hasLength(content) ? encryptor.encrypt(content) : content;
    }

    /**
     * 解密请求内容
     *
     * @param content 解密内容
     * @return 解密结果
     */
    public static String decrypt(@NotNull String content) {
        TextEncryptor encryptor = getEncryptor();
        return StringUtils.hasLength(content) ? encryptor.decrypt(content) : content;
    }

    /**
     * 认证相关参数加密
     *
     * @param principal   登录主体，例如：手机号码、用户名、邮箱等
     * @param credentials 登录证书，例如：密码、验证码等
     * @return 认证参数
     */
    public static AuthenticationParameter encryptAuthenticationParameter(@NotBlank String principal, @NotBlank String credentials) {
        return new AuthenticationParameter(encrypt(principal), encrypt(credentials));
    }

    /**
     * 解密认证相关参数
     *
     * @param principal   登录主体，例如：手机号码、用户名、邮箱等
     * @param credentials 登录验证密码
     * @return 认证参数
     */
    public static AuthenticationParameter decryptAuthenticationParameter(@NotBlank String principal, @NotBlank String credentials) {
        return new AuthenticationParameter(decrypt(principal), decrypt(credentials));
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        try {
            WindAuthenticationProperties properties = event.getApplicationContext().getBean(WindAuthenticationProperties.class);
            KeyPair keyPair = properties.getKeyPair();
            TEXT_ENCRYPTOR.set(RasTextEncryptor.ofPublicEncrypt(keyPair.getPublic(), keyPair.getPrivate()));
        } catch (BeansException e) {
            log.info("un enable authentication parameter crypto");
        }
    }

    private static TextEncryptor getEncryptor() {
        TextEncryptor encryptor = TEXT_ENCRYPTOR.get();
        AssertUtils.notNull(encryptor, "un configure parameter encryptor");
        return encryptor;
    }

    /**
     * @param principal   登录主体，例如：手机号码、用户名、邮箱等
     * @param credentials 登录证书，例如：密码、验证码等
     */
    public record AuthenticationParameter(String principal, String credentials) {

    }
}
