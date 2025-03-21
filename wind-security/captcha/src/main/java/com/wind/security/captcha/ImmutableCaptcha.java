package com.wind.security.captcha;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.beans.Transient;

/**
 * @author wuxp
 * @date 2023-09-24 09:48
 **/
@Getter
@Builder
@AllArgsConstructor
public class ImmutableCaptcha implements Captcha {

    /**
     * 验证码所有者
     */
    private final String owner;

    /**
     * 验证码类型
     */
    private final CaptchaType type;

    /**
     * 验证码使用场景
     */
    private final CaptchaUseScene useScene;

    /**
     * 验证码值
     */
    private final String value;

    /**
     * 验证码内容
     */
    private final String content;

    /**
     * 已验证次数
     */
    private final int verificationCount;

    /**
     * 已发送次数
     */
    private final int sendTimes;

    /**
     * 允许验证次数
     */
    private final int allowVerificationTimes;

    /**
     * 过期时间
     */
    private final long expireTime;

    /**
     * 为了给序列化框架使用，提供一个空构造
     */
    ImmutableCaptcha() {
        this(null, null, null, null, null, 0, 1, 0, 0);
    }

    /**
     * 统计已验证次数
     *
     * @return 新的验证码
     */
    public ImmutableCaptcha increaseSendTimes() {
        // 重新发送，过期时间延长 3 分钟
        return new ImmutableCaptcha(owner, type, useScene, value, content, verificationCount, sendTimes + 1, allowVerificationTimes,
                expireTime + 3 * 60 * 1000);
    }

    /**
     * 统计已验证次数
     *
     * @return 新的验证码
     */
    public ImmutableCaptcha increaseVerificationCount() {
        return new ImmutableCaptcha(owner, type, useScene, value, content, verificationCount + 1, sendTimes, allowVerificationTimes, expireTime);
    }

    /**
     * 不进行持久化
     */
    @Transient
    public String getContent() {
        return content;
    }
}
