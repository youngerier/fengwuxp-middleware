package com.wind.websocket.chat;

import com.wind.common.enums.DescriptiveEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 聊天会话类型枚举
 *
 * @author wuxp
 * @date 2025-05-30 10:45
 **/
@AllArgsConstructor
@Getter
public enum ChatSocketSessionType implements DescriptiveEnum {

    /**
     * 群聊
     */
    GROUP("群聊"),

    /**
     * 私聊
     */
    PRIVATE("私聊"),

    /**
     * 消息通知
     */
    NOTIFICATION("消息通知"),

    ;

    private final String desc;
}
