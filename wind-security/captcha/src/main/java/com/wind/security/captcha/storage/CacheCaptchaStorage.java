package com.wind.security.captcha.storage;

import com.wind.common.WindConstants;
import com.wind.common.exception.AssertUtils;
import com.wind.security.captcha.Captcha;
import com.wind.security.captcha.CaptchaConstants;
import com.wind.security.captcha.CaptchaStorage;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.NonNull;

/**
 * @param group 业务模块分组
 * @author wuxp
 * @date 2023-09-24 14:43
 */
public record CacheCaptchaStorage(CacheManager cacheManager, String group) implements CaptchaStorage {

    public CacheCaptchaStorage(CacheManager cacheManager) {
        this(cacheManager, WindConstants.DEFAULT_TEXT.toUpperCase());
    }

    @Override
    public void store(Captcha captcha) {
        requiredCache(captcha.type(), captcha.useScene()).put(captcha.owner(), captcha);
    }

    @Override
    public Captcha get(Captcha.CaptchaType type, Captcha.CaptchaUseScene useScene, String key) {
        return requiredCache(type, useScene).get(key, Captcha.class);
    }

    @Override
    public void remove(Captcha.CaptchaType type, Captcha.CaptchaUseScene useScene, String key) {
        requiredCache(type, useScene).evict(key);
    }

    @NonNull
    private Cache requiredCache(Captcha.CaptchaType captchaTyp, Captcha.CaptchaUseScene useScene) {
        String name = CaptchaConstants.getCaptchaCacheName(group, captchaTyp, useScene);
        Cache result = cacheManager.getCache(name);
        AssertUtils.notNull(result, String.format("获取验证码 Cache 失败，CacheName = %s", name));
        return result;
    }
}

