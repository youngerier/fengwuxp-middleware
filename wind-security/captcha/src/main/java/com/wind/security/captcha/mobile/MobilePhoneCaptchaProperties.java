package com.wind.security.captcha.mobile;

import com.wind.security.captcha.Captcha;
import lombok.Data;

import java.time.Duration;

/**
 * @author wuxp
 * @date 2023-09-24 12:42
 **/
@Data
public class MobilePhoneCaptchaProperties implements Captcha.CaptchaConfiguration {

    /**
     * 图片验证码长度
     */
    private int length = 6;

    /**
     * 验证码有效时长
     */
    private Duration effectiveTime = Duration.ofMinutes(10);

    /**
     * 最大可以验证次数
     */
    private int maxAllowVerificationTimes = 5;

    /**
     * 每个用户每天允许发生验证码的最大次数
     */
    private int mxAllowGenerateTimesOfUserWithDay = 15;
}
