package cn.fyupeng.factory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @Auther: fyp
 * @Date: 2022/3/26
 * @Description:
 * @Package: cn.fyupeng.factory
 * @Version: 1.0
 */
@Slf4j
public class ThreadPoolFactory {
    /**
     * CORE_POOL_SIZE : 线程池 CPU 核数
     * MAXIMUM_POOL_SIZE ： 最大 的 线程数
     * BLOCKING_QUEUE_CAPACITY ： 阻塞 队列 容量
     * KEEP_ALIVE_TIMEOUT ： 心跳（单位：每分钟）
     */

    private static final int FIXED_CORE_POOL_SIZE = 200;
    private static final int FIXED_MAXIMUM_POOL_SIZE = 200;
    private static final int FIXED_KEEP_ALIVE_TIMEOUT = 0;

    private static final int CACHE_CORE_POOL_SIZE = 0;
    private static final int CACHE_MAXIMUM_POOL_SIZE = 2147483647;
    private static final int CACHE_KEEP_ALIVE_TIMEOUT = 60000;

    public static final int FIXED_THREAD_POOL = 0;
    public static final int DEFAULT_THREAD_POOL = 1;
    public static final int CACHE_THREAD_POOL = 2;

    private static Map<String, ExecutorService> threadPoolsMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        ExecutorService test = createDefaultThreadPool("test");
        System.out.println(test);
    }

    public static ExecutorService createDefaultThreadPool(String threadNamePrefix) {
        return createFixedThreadPool(threadNamePrefix, 0, false);
    }

    /**
     * @param schema DEFAULT_THREAD_POOL、FIXED_THREAD_POOL、CACHE_THREAD_POOL
     * @param threadNamePrefix 线程池命名，用于线程池的复用
     * @param queues 是否为守护线程
     * @return
     */
    public static ExecutorService createThreadPool(int schema, String threadNamePrefix, int queues) {
        return createThreadPool0(schema, threadNamePrefix, queues, false);
    }

    /**
     * @param schema DEFAULT_THREAD_POOL、FIXED_THREAD_POOL、CACHE_THREAD_POOL
     * @param threadNamePrefix 线程池命名，用于线程池的复用
     * @param queues 任务队列长度，根据长度选型，小于 0 为 链队列，等于0 为无容量队列，大于0 为
     * @param daemon 是否为守护线程
     * @return
     */
    public static ExecutorService createThreadPool(int schema, String threadNamePrefix, int queues, boolean daemon) {
        return createThreadPool0(schema, threadNamePrefix, queues, daemon);
    }

    private static ExecutorService createThreadPool0(int schema, String threadNamePrefix, int queues, Boolean daemon) {
        /**
         * 第一次有效，下次 返回 首次值, 参数 2 支持 函数编程
         * @FunctionalInterface
         * public interface Function<T, R> {
         *  R apply(T t);
         *  k 代表 apply(T t) 中的 参数 t
         *  createThreadPool(threadNamePrefix, daemon) 代表 R 类型的返回值 即 Map<K,V> 的 V
         *  因为 Function<? super K, ? extends V> K 对应了 T , V 对应了 R
         *  而 public interface Map<K, V> {
         */
        ExecutorService pool = null;
        switch (schema) {
            case DEFAULT_THREAD_POOL :
            case FIXED_THREAD_POOL: {
                pool = threadPoolsMap.computeIfAbsent(threadNamePrefix, k -> createFixedThreadPool(threadNamePrefix, queues, daemon));
                break;
            }
            case CACHE_THREAD_POOL: {
                pool = threadPoolsMap.computeIfAbsent(threadNamePrefix, k -> createCacheThreadPool(threadNamePrefix, queues, daemon));
                break;
            }
        }
        if (pool != null && (pool.isShutdown() || pool.isTerminated())) {
            threadPoolsMap.remove(threadNamePrefix);
            if (schema == FIXED_THREAD_POOL) {
                pool = createFixedThreadPool(threadNamePrefix, queues, daemon);
            } else if (schema == CACHE_THREAD_POOL) {
                pool = createCacheThreadPool(threadNamePrefix, queues, daemon);
            } else {
                pool = createFixedThreadPool(threadNamePrefix, queues, daemon);
            }
            threadPoolsMap.put(threadNamePrefix, pool);
        }
        return pool;
    }

    private static ExecutorService createFixedThreadPool(String threadNamePrefix, int queues, boolean daemon) {
        BlockingQueue workQueue = queues == 0 ? new SynchronousQueue() : (queues < 0 ? new LinkedBlockingQueue() : new LinkedBlockingQueue(queues));
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
        return new ThreadPoolExecutor(FIXED_CORE_POOL_SIZE, FIXED_MAXIMUM_POOL_SIZE, FIXED_KEEP_ALIVE_TIMEOUT, TimeUnit.MILLISECONDS, workQueue, threadFactory);
    }

    private static ExecutorService createCacheThreadPool(String threadNamePrefix, int queues, boolean daemon) {
        BlockingQueue workQueue = queues == 0 ? new SynchronousQueue() : (queues < 0 ? new LinkedBlockingQueue() : new LinkedBlockingQueue(queues));
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
        return new ThreadPoolExecutor(CACHE_CORE_POOL_SIZE, CACHE_MAXIMUM_POOL_SIZE, CACHE_KEEP_ALIVE_TIMEOUT, TimeUnit.MILLISECONDS, workQueue, threadFactory);
    }

    public static void shutdownAll() {
        log.info("close all ThreadPool now ...");
        threadPoolsMap.entrySet().parallelStream().forEach(entry -> {
            ExecutorService executorService = entry.getValue();
            // 不接受 新任务，等待 现有 任务 执行完毕 后 关闭
            executorService.shutdown();
            log.info("close threadPool [{}] [{}]", entry.getKey(), executorService.isTerminated());
            try {
                // 所以 这里 要阻塞 等待 任务 执行完
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("failed to close thread pool: ",e);
                // 使用 中断 操作 去尝试 关闭所有 正在执行的 任务
                executorService.shutdownNow();
            }
        });
        log.info("threadPool closed successfully");
    }

    /**
     * 创建 ThreadFactory, 如果 threadNamePrefix 不为空则 使用 ThreadFactory, 否则 默认 创建 defaultThreadFactory
     * @param threadNamePrefix 线程名 前缀
     * @param daemon 指定 是否 为 守护线程
     * @return ThreadFactory
     */
    private static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        if (threadNamePrefix != null) {
            if (daemon != null) {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").setDaemon(daemon).build();
            } else {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();
            }
        }
        return Executors.defaultThreadFactory();
    }

}
