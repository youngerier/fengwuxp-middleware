package com.wind.websocket;

/**
 * websocket 相关元数据名称
 *
 * @author wuxp
 * @date 2025-05-30 16:25
 **/
public final class WindWebSocketMetadataNames {

    private WindWebSocketMetadataNames() {
        throw new AssertionError();
    }

    /**
     * 客户端id
     */
    public static final String CLIENT_ID_NAME = "clientId";

    /**
     * 客户端设备类型
     */
    public static final String CLIENT_DEVICE_TYPE_NAME = "clientDeviceType";

    /**
     * 客户端ip
     *
     */
    public static final String CLIENT_IP_NAME = "clientIp";

    /**
     * 用户id
     */
    public static final String USER_ID_NAME = "userId";

    /**
     * 用户代理
     */
    public static final String USER_AGENT_NAME = "userAgent";

    /**
     * 连接时间
     */
    public static final String GMT_CONNECTED_NAME = "gmtConnected";
}
