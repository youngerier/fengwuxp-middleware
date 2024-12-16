package com.wind.security;

/**
 * @author wuxp
 * @date 2023-10-04 18:53
 **/
public final class WebSecurityConstants {

    private WebSecurityConstants() {
        throw new AssertionError();
    }


    /**
     * 保存在请求上线文中，当前请求需要的权限
     */
    public static final String REQUEST_REQUIRED_AUTHORITIES_ATTRIBUTE_NAME = "REQUEST_REQUIRED_AUTHORITIES";

    /**
     * 登录已失效
     */
    public static final String LOGIN_JWT_TOKEN_INVALID = "登录凭证不存在或已过期";
}
