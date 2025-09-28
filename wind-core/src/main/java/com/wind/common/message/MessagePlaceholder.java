package com.wind.common.message;

/**
 * 消息占位符描述
 *
 * @param pattern 消息占位符表达式
 * @param args    占位符参数
 * @author wuxp
 * @date 2023-10-30 07:41
 */
public record MessagePlaceholder(String pattern, Object[] args) {

    public static MessagePlaceholder of(String pattern, Object... args) {
        return new MessagePlaceholder(pattern, args == null ? new Object[0] : args);
    }
}
