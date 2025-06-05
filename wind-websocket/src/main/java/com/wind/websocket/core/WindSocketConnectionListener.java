package com.wind.websocket.core;

/**
 * 连接监听器
 *
 * @author wuxp
 * @date 2025-05-30 16:22
 **/
public interface WindSocketConnectionListener {

    /**
     * 连接建立成功
     *
     * @param connection 连接
     */
    void onConnect(WindSocketClientClientConnection connection);

    /**
     * 断开连接
     *
     * @param connection 连接
     */
    void onDisconnect(WindSocketClientClientConnection connection);

    /**
     * 连接异常
     *
     * @param connection 连接
     * @param throwable  异常信息
     */
    void onError(WindSocketClientClientConnection connection, Throwable throwable);
}
