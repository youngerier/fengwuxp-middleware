package com.wind.common.utils;

import com.wind.common.WindConstants;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 使用逗号分隔裁剪或连接字符串
 *
 * @author wuxp
 * @date 2023-10-23 11:05
 **/
public final class StringJoinSpiltUtils {

    private StringJoinSpiltUtils() {
        throw new AssertionError();
    }

    public static Set<String> spilt(String text) {
        if (StringUtils.hasText(text)) {
            return new HashSet<>(Arrays.asList(text.split(WindConstants.COMMA)));
        }
        return new HashSet<>();
    }

    public static Set<Long> spiltAsLong(String text) {
        if (StringUtils.hasText(text)) {
            return Arrays.stream(text.split(WindConstants.COMMA)).map(Long::parseLong).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    @Nullable
    public static String joinEnums(Collection<? extends Enum<?>> enums) {
        return join(enums.stream().map(Enum::name).collect(Collectors.toList()));
    }

    @Nullable
    public static String joinEnums(Enum<?>... enums) {
        return join(Arrays.asList(enums));
    }

    @Nullable
    public static String join(Object... objects) {
        return join(Arrays.asList(objects));
    }

    @Nullable
    public static String join(Collection<?> texts) {
        if (texts == null) {
            return null;
        }
        return texts.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(WindConstants.COMMA));
    }
}
