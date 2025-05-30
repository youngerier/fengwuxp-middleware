package com.wind.websocket.core;

import com.wind.common.exception.AssertUtils;
import com.wind.websocket.WindWebSocketMetadataNames;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * websocket 客户端连接
 *
 * @author wuxp
 * @date 2025-05-30 15:39
 **/
public interface WindSocketClientConnection {

    /**
     * @return 获取连接 id
     */
    @NotBlank
    String getId();

    /**
     * @return 获取连接关联的会话 id
     */
    String getSessionId();

    /**
     * @return 客户端唯一标识，可以在创建连接时通过参数传递
     */
    @Null
    default String getClientId() {
        return getMetadataValue(WindWebSocketMetadataNames.CLIENT_ID_NAME);
    }

    /**
     * @return 客户端设备类型
     */
    default String getClientDeviceType() {
        return getMetadataValue(WindWebSocketMetadataNames.CLIENT_DEVICE_TYPE_NAME);
    }

    /**
     * @return 连接关联的用户 id
     */
    @Null
    default String getUserId() {
        return getMetadataValue(WindWebSocketMetadataNames.USER_ID_NAME);
    }

    /**
     * @return 获取用户代理信息
     */
    default String getUserAgent() {
        return getMetadataValue(WindWebSocketMetadataNames.USER_AGENT_NAME);
    }

    /**
     * @return 获取客户端ip
     */
    @NotBlank
    default String getClientIp() {
        return requireMetadataValue(WindWebSocketMetadataNames.CLIENT_IP_NAME);
    }

    /**
     * @return 是否已认证
     */
    default boolean isAuthenticated() {
        return getUserId() != null;
    }

    /**
     * @return 连接建立时间
     */
    @NotNull
    default LocalDateTime getGmtConnected() {
        return requireMetadataValue(WindWebSocketMetadataNames.GMT_CONNECTED_NAME);
    }

    /**
     * 发送文本消息
     *
     * @param message 消息内容
     */
    default void sendText(@NotNull String message) {
        send(message);
    }

    /**
     * 发送消息
     *
     * @param payload 消息负载
     */
    void send(@NotNull Object payload);

    /**
     * 关闭连接
     */
    void close();

    /**
     * @return 连接是否存活
     */
    boolean isAlive();

    /**
     * @return 获取连接的元数据
     */
    @NotNull
    Map<String, Object> getMetadata();

    @SuppressWarnings("unchecked")
    @Null
    default <T> T getMetadataValue(String key) {
        return (T) getMetadata().get(key);
    }

    @NotNull
    default <T> T requireMetadataValue(String key) {
        T result = getMetadataValue(key);
        AssertUtils.notNull(result, () -> String.format("metadata key = %s must not null", key));
        return result;
    }
}
