package com.wind.websocket.core;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

/**
 * socket 会话管理服务
 *
 * @author wuxp
 * @date 2025-05-30 10:41
 **/
public interface WindSocketSessionManager {

    /**
     * 创建会话
     *
     * @return 会话
     */
    default WindSocketSession createSession() {
        return createSession(null);
    }

    /**
     * 创建会话
     *
     * @param name 会话名称
     * @return 会话
     */
    default WindSocketSession createSession(@Null String name) {
        return createSession(null, name);
    }

    /**
     * 创建会话
     *
     * @param sessionId 指定的会话 id
     * @param name      会话名称
     * @return 会话 id
     */
    WindSocketSession createSession(@Null String sessionId, @Null String name);

    /**
     * 更新会话名称
     *
     * @param sessionId 会话 id
     * @param newName   新的会话名称
     */
    void updateSessionName(@NotBlank String sessionId, @NotBlank String newName);

    /**
     * 获取会话
     *
     * @param sessionId 会话 id
     * @return 会话信息
     */
    @NotNull
    WindSocketSession getSession(@NotBlank String sessionId);

    /**
     * 销毁一个会话
     *
     * @param sessionId 会话 id
     */
    void destroySession(@NotBlank String sessionId);

    /**
     * 判断会话是否存在
     *
     * @param sessionId 会话 id
     * @return 是否存在
     */
    boolean exists(@NotBlank String sessionId);
}
