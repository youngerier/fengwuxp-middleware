package com.wind.common.query.cursor;

import com.wind.common.WindConstants;
import com.wind.common.exception.AssertUtils;
import com.wind.common.exception.BaseException;
import com.wind.common.exception.DefaultExceptionCode;
import com.wind.common.util.WindReflectUtils;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author wuxp
 * @date 2025-09-30 13:12
 **/
final class QueryCursorUtils {

    private QueryCursorUtils() {
        throw new AssertionError();
    }

    /**
     * 生成 cursor
     *
     * @param query        查询参数
     * @param lastRecordId 最后一条记录的 id
     * @return cursor
     */
    @SuppressWarnings("rawtypes")
    static String generateCursor(@NotNull AbstractCursorQuery query, @NotNull Object lastRecordId) {
        AssertUtils.notNull(query, "argument query must not null");
        AssertUtils.notNull(lastRecordId, "argument lastRecordId must not null");
        return genCursorSha256(query) + WindConstants.AT + lastRecordId;
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    static String checkCursorAndGetLastRecordId(AbstractCursorQuery query) {
        String cursor = query.getCursor();
        if (cursor == null) {
            return null;
        }
        String[] parts = cursor.split(WindConstants.AT);
        if (parts.length != 2) {
            throw new BaseException(DefaultExceptionCode.COMMON_FRIENDLY_ERROR, "cursor 格式错误");
        }
        String signature = parts[0];
        AssertUtils.equals(genCursorSha256(query), signature, "cursor 签名错误");
        return StringUtils.hasText(parts[1]) ? parts[1] : null;
    }

    @SuppressWarnings("rawtypes")
    private static String genCursorSha256(AbstractCursorQuery query) {
        Field[] fields = WindReflectUtils.getFields(query.getClass());
        String queryString = Arrays.stream(fields)
                .filter(field -> !Objects.equals(field.getName(), "cursor"))
                .sorted((o1, o2) -> o1.getName().compareTo(o2.getName()))
                .map(field -> field.getName() + WindConstants.EQ + WindReflectUtils.getFieldValue(field, query))
                .collect(Collectors.joining(WindConstants.AND));
        return sha256Base64(queryString);
    }

    private static String sha256Base64(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new BaseException(DefaultExceptionCode.COMMON_FRIENDLY_ERROR, "SHA-256 加密失败", exception);
        }
    }
}
