package com.wind.common.executor;

import com.wind.common.limit.WindExecutionLimiter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

/**
 * 基于 https://github.com/bucket4j/bucket4j 实现的令牌桶/漏桶 任务限流器
 *
 * @author wuxp
 * @date 2025-06-09 11:03
 **/
public final class Bucket4jTaskExecutionLimiterFactory {

    private static final AtomicReference<BiFunction<String, Bandwidth, WindExecutionLimiter>> LIMITER_FACTORY =
            new AtomicReference<>((name, limit) -> {
                Bucket bucket = Bucket.builder()
                        .addLimit(limit)
                        .build();
                return (resourceKey, args) -> bucket.tryConsume(1);
            });

    private Bucket4jTaskExecutionLimiterFactory() {
        throw new AssertionError();
    }

    /**
     * 令牌桶，支持累计（（上限为桶的容量 {@param capacity}）
     *
     * @param capacity     桶的容量
     * @param refillTokens 填充的令牌数
     * @param refillPeriod 填充的间隔时间
     * @return ExecuteTaskRateLimiter
     */
    public static WindExecutionLimiter tokenBucket(int capacity, int refillTokens, Duration refillPeriod) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(refillTokens, refillPeriod)
                .build();
        return LIMITER_FACTORY.get().apply("bucket4j.token.default-bucket", limit);
    }

    public static WindExecutionLimiter tokenBucket(int capacity, int tokenPerSecond) {
        return tokenBucket(capacity, tokenPerSecond, Duration.ofSeconds(1));
    }

    public static WindExecutionLimiter tokenWithSingleSeconds(int tokenPerSecond) {
        return tokenBucket(tokenPerSecond, tokenPerSecond);
    }

    public static WindExecutionLimiter tokenWithSingleSeconds() {
        return tokenBucket(1, 1);
    }

    /**
     * 漏桶，匀速填充，不支持累计 （上限为桶的容量 {@param capacity}）
     *
     * @param capacity     桶的容量
     * @param refillTokens 填充的令牌数
     * @param refillPeriod 填充的间隔时间
     * @return ExecuteTaskRateLimiter
     */
    public static WindExecutionLimiter leakyBucket(int capacity, int refillTokens, Duration refillPeriod) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(refillTokens, refillPeriod)
                .build();
        return LIMITER_FACTORY.get().apply("bucket4j.token.leaky-bucket", limit);
    }

    public static WindExecutionLimiter leakyBucket(int refillTokens, Duration refillPeriod) {
        return leakyBucket(refillTokens, refillTokens, refillPeriod);
    }

    public static WindExecutionLimiter leakyBucketWithSeconds(int tokenPerSecond) {
        return leakyBucket(tokenPerSecond, tokenPerSecond, Duration.ofSeconds(1));
    }

    public static WindExecutionLimiter leakyWithSingleSeconds() {
        return leakyBucketWithSeconds(1);
    }

    public static void setLimiterFactory(BiFunction<String, Bandwidth, WindExecutionLimiter> factory) {
        LIMITER_FACTORY.set(factory);
    }
}
