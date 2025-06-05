package com.wind.common.util;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池创建工具
 *
 * @author wuxp
 * @date 2023-12-26 10:35
 **/
public final class ExecutorServiceUtils {

    private ExecutorServiceUtils() {
        throw new AssertionError();
    }

    /**
     * 创建一个单线程，等待队列为 128 的线程池
     *
     * @param threadNamePrefix 线程池名称前缀
     * @return 线程池
     */
    public static ThreadPoolExecutor single(String threadNamePrefix) {
        return single(threadNamePrefix, 128);
    }

    /**
     * 创建一个单线程的线程池
     *
     * @param threadNamePrefix 线程池名称前缀
     * @param workQueueSize    等待队列大小
     * @return 线程池
     */
    public static ThreadPoolExecutor single(String threadNamePrefix, int workQueueSize) {
        return newExecutor(threadNamePrefix, 1, 1, workQueueSize);
    }

    /**
     * 创建线程池
     *
     * @param threadNamePrefix 线程池名称前缀
     * @param corePoolSize     核心线程数
     * @param maximumPoolSize  最大线程数
     * @param workQueueSize    等待队列大小 默认使用 {@link ArrayBlockingQueue}
     * @return 线程池
     */
    public static ThreadPoolExecutor newExecutor(String threadNamePrefix, int corePoolSize, int maximumPoolSize, int workQueueSize) {
        return newExecutor(threadNamePrefix, corePoolSize, maximumPoolSize, new ArrayBlockingQueue<>(workQueueSize));
    }

    private static ThreadPoolExecutor newExecutor(String threadNamePrefix, int corePoolSize, int maximumPoolSize, BlockingQueue<Runnable> workQueue) {
        return ExecutorBuilder.withThreadName(threadNamePrefix)
                .corePoolSize(corePoolSize)
                .maximumPoolSize(maximumPoolSize)
                .workQueue(workQueue)
                .build();
    }

    public static class ExecutorBuilder {

        private String threadNamePrefix;

        private int corePoolSize = 1;

        private int maximumPoolSize = 1;

        /**
         * 默认线程存活时间 1 分钟
         */
        private Duration keepAlive = Duration.ofMinutes(1);

        private BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(128);

        private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();

        private ExecutorBuilder() {
        }

        public static ExecutorBuilder withThreadName(String threadNamePrefix) {
            ExecutorBuilder result = new ExecutorBuilder();
            result.threadNamePrefix = threadNamePrefix;
            return result;
        }

        public ExecutorBuilder corePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
            return this;
        }

        public ExecutorBuilder maximumPoolSize(int maximumPoolSize) {
            this.maximumPoolSize = maximumPoolSize;
            return this;
        }

        public ExecutorBuilder keepAlive(Duration keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }

        public ExecutorBuilder workQueue(BlockingQueue<Runnable> workQueue) {
            this.workQueue = workQueue;
            return this;
        }

        public ExecutorBuilder rejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
            this.rejectedExecutionHandler = rejectedExecutionHandler;
            return this;
        }

        public ThreadPoolExecutor build() {
            return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAlive.getSeconds(), TimeUnit.SECONDS, workQueue,
                    new CustomizableThreadFactory(threadNamePrefix), rejectedExecutionHandler);
        }
    }
}
