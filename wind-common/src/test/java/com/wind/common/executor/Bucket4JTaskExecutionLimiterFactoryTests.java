package com.wind.common.executor;

import com.wind.common.limit.WindExecutionLimiter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.awaitility.Awaitility.await;

/**
 * @author wuxp
 * @date 2025-06-09 11:11
 **/
class Bucket4JTaskExecutionLimiterFactoryTests {

    @Test
    void testLeakyBucketWithCapacity3Fill1BySec() {
        WindExecutionLimiter rateLimiter = Bucket4jTaskExecutionLimiterFactory.leakyBucket(3, 1, Duration.ofMillis(500));
        Assertions.assertTrue(rateLimiter.tryAcquire());
        Assertions.assertTrue(rateLimiter.tryAcquire());
        Assertions.assertTrue(rateLimiter.tryAcquire());
        Assertions.assertFalse(rateLimiter.tryAcquire());
        // 等待下一个令牌补充完成
        await().atMost(Duration.ofMillis(550))
                .until(rateLimiter::tryAcquire);
        // 立即再试，应该失败（桶只有一个令牌）
        Assertions.assertFalse(rateLimiter.tryAcquire());
    }

    @Test
    void testLeakyBucketWithCapacity3Fill1ByMillis() {
        WindExecutionLimiter rateLimiter = Bucket4jTaskExecutionLimiterFactory.leakyBucket(3, 1, Duration.ofMillis(20));
        Assertions.assertTrue(rateLimiter.tryAcquire());
        Assertions.assertTrue(rateLimiter.tryAcquire());
        Assertions.assertTrue(rateLimiter.tryAcquire());
        Assertions.assertFalse(rateLimiter.tryAcquire());
        // 等待下一个令牌补充完成
        await().atMost(Duration.ofMillis(110))
                .until(rateLimiter::tryAcquire);
        // 立即再试，应该失败（桶只有3个令牌）
        Assertions.assertTrue(rateLimiter.tryAcquire());
        Assertions.assertTrue(rateLimiter.tryAcquire());
        Assertions.assertFalse(rateLimiter.tryAcquire());
    }

    @Test
    void testTokenBucket() {
        WindExecutionLimiter rateLimiter = Bucket4jTaskExecutionLimiterFactory.tokenWithSingleSeconds();
        Assertions.assertTrue(rateLimiter.tryAcquire());
        Assertions.assertFalse(rateLimiter.tryAcquire());
        // 等待下一个令牌补充完成
        await().atMost(Duration.ofMillis(1100))
                .until(rateLimiter::tryAcquire);
        // 立即再试，应该失败（桶只有一个令牌）
        Assertions.assertFalse(rateLimiter.tryAcquire());
    }

    @Test
    void testTokenBucketWithCapacity10Fill1ByMillis() {
        int capacity = 10;
        WindExecutionLimiter rateLimiter = Bucket4jTaskExecutionLimiterFactory.leakyBucket(capacity, 1, Duration.ofMillis(20));
        for (int i = 0; i < capacity; i++) {
            Assertions.assertTrue(rateLimiter.tryAcquire());
        }
        Assertions.assertFalse(rateLimiter.tryAcquire());
        // 等待下一个令牌补充完成
        await().atMost(Duration.ofMillis(110))
                .until(rateLimiter::tryAcquire);
        Assertions.assertTrue(rateLimiter.tryAcquire());
        Assertions.assertTrue(rateLimiter.tryAcquire());
        Assertions.assertTrue(rateLimiter.tryAcquire());
        Assertions.assertTrue(rateLimiter.tryAcquire());
        Assertions.assertFalse(rateLimiter.tryAcquire());
    }
}
