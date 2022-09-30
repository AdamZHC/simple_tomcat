package com.hit.adam.tomcat.connector.threadpool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleThreadFactory implements ThreadFactory {
    /**
     * 原子类控制并发
     */
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    /**
     * 简单命名规范
     */
    private final String namePrefix;

    public SimpleThreadFactory() {
        namePrefix = "pool-" +
                poolNumber.getAndIncrement() +
                "-thread-";
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(r,
                namePrefix + threadNumber.getAndIncrement());
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }

    /**
     * 命名的新方法
     * @param r: 执行的新任务
     * @return: 获取新的线程
     */
    public Thread getInstanceOfThread(Runnable r) {
        return newThread(r);
    }
}
