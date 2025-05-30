package com.wind.websocket.chat;

import com.wind.common.enums.DescriptiveEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息内容类型
 *
 * @author wuxp
 * @date 2025-05-27 10:38
 **/
@AllArgsConstructor
@Getter
public enum ChatMessageContentType implements DescriptiveEnum {

    TEXT("text"),

    IMAGE("image"),

    VIDEO("video"),

    AUDIO("audio"),

    FILE("file");

    private final String desc;
}
