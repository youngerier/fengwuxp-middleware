package com.wind.websocket.chat;

import com.wind.websocket.core.WindSocketSession;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

/**
 * 聊天会话（支持一对一、群聊、系统广播等场景））
 *
 * @author wuxp
 * @date 2025-05-30 10:39
 **/
public interface ChatSocketSession extends WindSocketSession {

    /***
     * @return 会话名称
     */
    @Null
    String getAvatar();

    /**
     * @return 会话拥有者 id
     */
    @NotNull
    String getOwnerId();

    /**
     * @return 会话类型
     */
    @NotNull
    ChatSocketSessionType getSessionType();

}
