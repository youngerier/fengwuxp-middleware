package com.wind.common.executor;

import com.wind.common.exception.BaseException;
import com.wind.common.exception.DefaultExceptionCode;
import com.wind.common.limit.WindExecutionLimiter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

import java.time.Duration;

/**
 * 限流任务装饰器
 *
 * @author wuxp
 * @date 2025-06-10 09:48
 **/
@AllArgsConstructor
@Slf4j
public class RateLimitTaskDecorator implements TaskDecorator {

    private final String taskName;

    private final WindExecutionLimiter limiter;

    private final Duration maxWait;

    /**
     * 创建限流任务装饰器（匀速执行），默认等待时间 200 毫秒
     *
     * @param taskName       任务名称
     * @param tokenPerSecond 每秒执行数
     * @param maxWait        最大等待时间
     * @return 限流任务装饰器
     */
    public static RateLimitTaskDecorator leaky(String taskName, int tokenPerSecond, Duration maxWait) {
        return new RateLimitTaskDecorator(taskName, Bucket4jTaskExecutionLimiterFactory.leakyBucketWithSeconds(tokenPerSecond), maxWait);
    }

    public static RateLimitTaskDecorator leaky(String taskName, int tokenPerSecond) {
        return leaky(taskName, tokenPerSecond, Duration.ofMillis(500));
    }

    /**
     * 创建限流任务装饰器（令牌桶），默认等待时间 200 毫秒
     *
     * @param taskName       任务名称
     * @param capacity       容量
     * @param tokenPerSecond token 每秒填充数
     * @param maxWait        最大等待时间
     * @return 限流任务装饰器
     */
    public static RateLimitTaskDecorator token(String taskName, int capacity, int tokenPerSecond, Duration maxWait) {
        return new RateLimitTaskDecorator(taskName, Bucket4jTaskExecutionLimiterFactory.tokenBucket(capacity, tokenPerSecond), maxWait);
    }

    public static RateLimitTaskDecorator token(String taskName, int capacity, int tokenPerSecond) {
        return token(taskName, capacity, tokenPerSecond, Duration.ofMillis(500));
    }

    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        return () -> {
            if (limiter.tryAcquire(taskName, maxWait)) {
                log.debug("execute rate limit decorate task, name = {}", taskName);
                runnable.run();
            } else {
                throw new BaseException(DefaultExceptionCode.TO_MANY_REQUESTS, "task name  = " + taskName + " rate limit exceeded");
            }
        };
    }
}
