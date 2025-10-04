package com.wind.security.captcha;

import com.google.common.collect.ImmutableSet;
import com.wind.common.annotations.VisibleForTesting;
import com.wind.common.exception.AssertUtils;
import com.wind.common.exception.BaseException;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Set;

/**
 * @param verificationIgnoreCase 验证时忽略大小写
 * @author wuxp
 * @date 2023-09-24 10:13
 */
@Slf4j
public record DefaultCaptchaManager(Collection<CaptchaContentProvider> delegates, CaptchaStorage captchaStorage, CaptchaGenerateChecker generateChecker,
                                    boolean verificationIgnoreCase) implements CaptchaManager {

    /**
     * 生成时允许使用之前的值的验证码类型
     */
    @VisibleForTesting
    static final Set<Captcha.CaptchaType> ALLOW_USE_PREVIOUS_CAPTCHA_TYPES = ImmutableSet.of(SimpleCaptchaType.EMAIL,
            SimpleCaptchaType.MOBILE_PHONE);

    public DefaultCaptchaManager(Collection<CaptchaContentProvider> delegates, CaptchaStorage captchaStorage, boolean verificationIgnoreCase) {
        this(delegates, captchaStorage, (owner, type) -> {
        }, verificationIgnoreCase);
    }

    public DefaultCaptchaManager(Collection<CaptchaContentProvider> delegates, CaptchaStorage captchaStorage,
                                 CaptchaGenerateChecker generateChecker) {
        this(delegates, captchaStorage, generateChecker, true);
    }

    /**
     * 生成验证码
     *
     * @param type     验证码类型
     * @param useScene 验证码使用场景
     * @param owner    验证码所有者
     * @return 验证码
     */
    @Override
    public Captcha generate(Captcha.CaptchaType type, Captcha.CaptchaUseScene useScene, String owner) {
        // 检查是否允许生成验证码
        String realOwner = owner.trim();
        generateChecker.preCheck(realOwner, type);
        if (ALLOW_USE_PREVIOUS_CAPTCHA_TYPES.contains(type)) {
            // 允许在未失效之前允许重复使用
            Captcha prevCaptcha = captchaStorage.get(type, useScene, realOwner);
            if (prevCaptcha != null) {
                Captcha result = prevCaptcha.increaseSendTimes();
                if (result.isAvailable()) {
                    captchaStorage.store(result);
                    return result;
                }
            }
        }
        CaptchaContentProvider delegate = getDelegate(type, useScene);
        CaptchaValue captchaValue = delegate.getValue(realOwner, useScene);
        Captcha result = ImmutableCaptcha.builder()
                .content(captchaValue.content())
                .value(captchaValue.value())
                .owner(realOwner)
                .type(type)
                .useScene(useScene)
                .expireTime(System.currentTimeMillis() + delegate.getEffectiveTime().toMillis() + 1500)
                .verificationCount(0)
                .sendTimes(1)
                .allowVerificationTimes(delegate.getMaxAllowVerificationTimes())
                .build();
        captchaStorage.store(result);
        return result;
    }

    /**
     * 验证验证码
     *
     * @param expected 预期的值
     * @param type     验证码类型
     * @param useScene 验证码使用场景
     * @param owner    验证码所有者
     */
    @Override
    public void verify(String expected, Captcha.CaptchaType type, Captcha.CaptchaUseScene useScene, String owner) {
        String realOwner = owner.trim();
        Captcha captcha = captchaStorage.get(type, useScene, realOwner);
        AssertUtils.notNull(captcha, CaptchaI18nMessageKeys.getCaptchaNotExistOrExpired(type));
        if (!captcha.isAvailable()) {
            // 验证码已失效，移除
            captchaStorage.remove(type, useScene, realOwner);
            throw BaseException.common(CaptchaI18nMessageKeys.getCaptchaNotExistOrExpired(type));
        }
        boolean isPass = verificationIgnoreCase ? captcha.value().equalsIgnoreCase(expected.trim()) : captcha.value().equals(expected.trim());
        if (isPass) {
            // 移除
            captchaStorage.remove(type, useScene, realOwner);
        } else {
            Captcha next = captcha.increaseVerificationCount();
            if (next.isAvailable()) {
                // 还可以继续用于验证，更新验证码
                captchaStorage.store(next);
            } else {
                // 验证码已失效，移除
                captchaStorage.remove(type, useScene, realOwner);
            }
            throw BaseException.common(CaptchaI18nMessageKeys.getCaptchaVerityFailure(type));
        }
    }

    private CaptchaContentProvider getDelegate(Captcha.CaptchaType type, Captcha.CaptchaUseScene scene) {
        for (CaptchaContentProvider delegate : delegates) {
            if (delegate.supports(type, scene)) {
                return delegate;
            }
        }
        throw BaseException.notFound(String.format("un found：type = %s，scene = %s CaptchaContentProvider", type.getDesc(), scene.getDesc()));
    }

}
