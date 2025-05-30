package com.wind.websocket.core;

import javax.validation.constraints.NotNull;
import java.net.URI;

/**
 * 路由连接
 *
 * @author wuxp
 * @date 2025-05-30 16:17
 **/
public interface WindSocketRouteConnection extends WindSocketClientConnection {

    /**
     * @return 远程节点地址
     */
    @NotNull
    URI getRemoteNodeAddress();

    /**
     * 通过 RPC 或则 http 的方式将消息转发到 {@link #getRemoteNodeAddress()} 节点
     *
     * @param payload 消息负载
     */
    @Override
    void send(Object payload);
}
