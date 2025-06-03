package com.wind.websocket.core;

import com.wind.common.enums.DescriptiveEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * websocket 会话类型枚举
 *
 * @author wuxp
 * @date 2025-05-30 10:45
 **/
@AllArgsConstructor
@Getter
public enum WindSocketSessionType implements DescriptiveEnum {

    DEFAULT("默认"),

    /**
     * 群聊
     */
    GROUP("群聊/群组"),

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
