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

    public static final String CLIENT_ID_NAME = "clientId";

    public static final String CLIENT_DEVICE_TYPE_NAME = "clientDeviceType";

    public static final String CLIENT_IP_NAME = "clientIp";

    public static final String USER_ID_NAME = "userId";

    public static final String USER_AGENT_NAME = "userAgent";

    /**
     * 连接时间
     */
    public static final String GMT_CONNECTED_NAME = "gmtConnected";
}
