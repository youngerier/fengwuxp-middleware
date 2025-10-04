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

    TEXT("Text"),

    IMAGE("Image"),

    VIDEO("Video"),

    AUDIO("Audio"),

    FILE("File");

    private final String desc;
}
