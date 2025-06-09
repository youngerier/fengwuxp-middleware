package com.wind.common.executor;

import com.wind.common.exception.AssertUtils;
import com.wind.common.limit.WindExecutionLimiter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 限流执行器
 *
 * @author wuxp
 * @date 2025-06-09 08:57
 **/
@AllArgsConstructor
@Slf4j
public class RateLimitExecutorService implements ExecutorService {

    private final ExecutorService delegate;

    private final WindExecutionLimiter limiter;

    /**
     * 创建限流执行器
     *
     * @param delegate       真正的线程执行器
     * @param tokenPerSecond 每秒执行的速率
     * @return 限流执行器
     */
    public RateLimitExecutorService ofSeconds(ExecutorService delegate, int tokenPerSecond) {
        return new RateLimitExecutorService(delegate, Bucket4jTaskExecutionLimiterFactory.tokenWithSingleSeconds(tokenPerSecond));
    }

    @Override
    public void execute(Runnable command) {
        tryAcquire();
        delegate.execute(command);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        tryAcquire();
        return delegate.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return submit(Executors.callable(task, result));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return submit(Executors.callable(task));
    }

    // invokeAll / invokeAny 仍按 BLOCK 执行，生产使用可增强支持
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        List<Callable<T>> wrapped = wrapTasks(tasks);
        return delegate.invokeAll(wrapped);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        List<Callable<T>> wrapped = wrapTasks(tasks);
        return delegate.invokeAll(wrapped, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        List<Callable<T>> wrapped = wrapTasks(tasks);
        return delegate.invokeAny(wrapped);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
            TimeoutException {
        List<Callable<T>> wrapped = wrapTasks(tasks);
        return delegate.invokeAny(wrapped, timeout, unit);
    }

    private <T> List<Callable<T>> wrapTasks(Collection<? extends Callable<T>> tasks) {
        List<Callable<T>> result = new ArrayList<>();
        for (Callable<T> task : tasks) {
            result.add(() -> {
                tryAcquire();
                return task.call();
            });
        }
        return result;
    }

    // 代理方法
    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }

    private void tryAcquire() {
        // 不阻塞，拿不到就 false
        AssertUtils.isTrue(limiter.tryAcquire(), "Execution rate is too high");
    }

}
