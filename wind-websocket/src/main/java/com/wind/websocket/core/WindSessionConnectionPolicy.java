package com.wind.websocket.core;

import com.wind.common.enums.DescriptiveEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 会话连接策略
 *
 * @author wuxp
 * @date 2025-05-30 17:05
 **/
@AllArgsConstructor
@Getter
public enum WindSessionConnectionPolicy implements DescriptiveEnum {

    DEFAULT("默认"),

    MULTI_DEVICE("多设备"),

    SINGLE_DEVICE_KICK_OLD("单设备，使用旧连接"),

    SINGLE_DEVICE_KICK_NEW("多设备, 使用新连接");

    private final String desc;
}
