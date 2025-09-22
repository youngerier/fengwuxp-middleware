package com.wind.security.authentication;

import com.wind.common.exception.AssertUtils;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication User
 *
 * @param id         用户 id
 * @param userName   用户名
 * @param attributes 用户属性
 * @author wuxp
 * @date 2023-10-26 12:49
 */
public record WindAuthenticationUser(String id, String userName, Map<String, Object> attributes) implements Serializable {

    @Serial
    private static final long serialVersionUID = -7270843983936135796L;

    public WindAuthenticationUser(Long id, String userName) {
        this(String.valueOf(id), userName);
    }

    public WindAuthenticationUser(String id, String userName) {
        this(id, userName, new HashMap<>());
    }

    /**
     * 获取长整型的 id
     *
     * @return id
     */
    public Long getIdAsLong() {
        return Long.parseLong(id);
    }

    @Deprecated
    public String getId() {
        return id;
    }

    @Deprecated
    public String getUserName() {
        return userName;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(@NotNull String key) {
        AssertUtils.notNull(key, "attribute name must not null");
        return (T) attributes.get(key);
    }

    /**
     * 获取属性并判断空
     *
     * @param key 属性名称
     * @return 属性值
     */
    public <T> T requireAttribute(@NotNull String key) {
        T result = getAttribute(key);
        AssertUtils.notNull(result, () -> String.format("attribute name = %s must not null", key));
        return result;
    }

    /**
     * 添加属性
     *
     * @param key   属性名称
     * @param value 属性值
     */
    public void putAttribute(@NotNull String key, Object value) {
        AssertUtils.notNull(key, "attribute name must not null");
        this.attributes.put(key, value);
    }
}
