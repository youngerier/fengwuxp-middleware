package com.wind.security.authentication;

/**
 * 认证 token id 和用户关系的 map
 *
 * @author wuxp
 * @date 2025-05-20 14:45
 **/
public interface AuthenticationTokenUserMap {

    /**
     * put token id 和用户 userId 的关系
     *
     * @param tokenId token id
     * @param userId  用户 id
     */
    void put(String tokenId, String userId);

    /**
     * 通过 token id 获取用户 userId
     *
     * @param tokenId token id
     */
    String getUserId(String tokenId);

    /**
     * 移除 token id
     *
     * @param tokenId token id
     */
    void remove(String tokenId);
}
