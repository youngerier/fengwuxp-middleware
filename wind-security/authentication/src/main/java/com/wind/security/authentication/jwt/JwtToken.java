package com.wind.security.authentication.jwt;

import com.wind.common.exception.AssertUtils;
import com.wind.common.exception.BaseException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * jwt token
 *
 * @author wuxp
 * @date 2023-09-26 09:35
 **/
@AllArgsConstructor
@Getter
public final class JwtToken implements Serializable {

    private static final long serialVersionUID = 1801730423764136024L;

    /**
     * token
     */
    private final String tokenValue;

    /**
     * jwt subject，一般是用户 ID
     */
    private final String subject;

    /**
     * 用户信息，如果是 refresh token，该字段为空
     */
    @Nullable
    private final JwtUser user;

    /**
     * token 过期时间戳
     */
    private final Long expireTime;

    @Deprecated
    public Long getUserId() {
        return Long.parseLong(subject);
    }

    /**
     * 获取长整型的 subject
     *
     * @return subject
     */
    public Long getSubjectAsLong() {
        return Long.parseLong(subject);
    }

    @NotNull
    public JwtUser requireUser() {
        AssertUtils.state(user != null, () -> BaseException.unAuthorized("user un authorized"));
        return user;
    }

    /**
     * @return 是否过期
     */
    public boolean isExpired() {
        return expireTime < System.currentTimeMillis();
    }
}
