package com.wind.security.authentication;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 鉴权 token 生成\解析验证服务
 *
 * @author wuxp
 * @date 2025-05-20 14:15
 **/
public interface AuthenticationTokenCodecService {

    /**
     * 通过用户创建一个 Authentication token
     *
     * @param userId     用户 id
     * @param userName   用户名
     * @param attributes 用户属性
     * @return Authentication token 对象
     */
    default WindAuthenticationToken generateToken(@NotNull Long userId, @NotBlank String userName, @NotNull Map<String, Object> attributes) {
        return generateToken(String.valueOf(userId), userName, attributes);
    }

    /**
     * 通过用户创建一个 Authentication token
     *
     * @param userId     用户 id
     * @param userName   用户名
     * @param attributes 用户属性
     * @return Authentication token 对象
     */
    WindAuthenticationToken generateToken(@NotBlank String userId, @NotBlank String userName, @NotNull Map<String, Object> attributes);

    /**
     * 通过用户创建一个 Authentication token
     *
     * @param userId 用户 id
     * @return Authentication token 对象
     */
    default WindAuthenticationToken generateRefreshToken(@NotNull Long userId) {
        return generateRefreshToken(String.valueOf(userId));
    }

    /**
     * 通过用户创建一个 Authentication token
     *
     * @param userId 用户 id
     * @return Authentication token 对象
     */
    WindAuthenticationToken generateRefreshToken(@NotBlank String userId);

    /**
     * 解析并检查 Authentication token
     *
     * @param accessToken Authentication token
     * @return Authentication token 对象
     */
    WindAuthenticationToken parseAndValidateToken(@NotBlank String accessToken);

    /**
     * 解析并检查 Authentication refresh token
     *
     * @param refreshToken Authentication token
     * @return Authentication token 对象
     */
    WindAuthenticationToken parseAndValidateRefreshToken(@NotBlank String refreshToken);

    /**
     * 撤销访问令牌，使其失效。
     *
     * @param accessToken 访问令牌字符串
     */
    void revokeAccessToken(@NotBlank String accessToken);

    /**
     * 撤销刷新令牌，使其无法继续使用。
     *
     * @param refreshToken 刷新令牌字符串
     */
    void revokeRefreshToken(@NotBlank String refreshToken);
}
