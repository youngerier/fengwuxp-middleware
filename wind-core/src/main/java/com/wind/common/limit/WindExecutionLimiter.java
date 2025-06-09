package com.wind.common.limit;

import com.wind.common.WindConstants;

/**
 * 执行限流器
 *
 * @author wuxp
 * @date 2025-06-09 15:40
 **/
@FunctionalInterface
public interface WindExecutionLimiter {

    default boolean tryAcquire() {
        return tryAcquire(WindConstants.UNKNOWN);
    }

    default boolean tryAcquire(String resourceKey) {
        return tryAcquire(resourceKey, new Object[0]);
    }

    /**
     * Acquires a permit from this if it can be acquired immediately without delay.
     *
     * @param resourceKey 资源标识
     * @param args        参与限流参数
     * @return {@code true} if the permit was acquired, {@code false} otherwise
     */
    boolean tryAcquire(String resourceKey, Object... args);
}
