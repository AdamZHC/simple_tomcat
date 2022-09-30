package com.hit.adam.tomcat.connector.threadpool;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 先按照自己的理解来写一写
 */
@SuppressWarnings("All")
public class SimpleThreadPoolExecutor implements Executor {
    /**
     * 线程状态的定义: 需要并发地修改
     * 实现方式: 通过上锁synchronized修改Status, 或者直接使用原子类
     */
    //没必要volatile实现可见性, 首先是安全性不一定能保证得到实现
    //因为工作内存对于抽象数据类型也有实现，可能也会导致错误，
    //另外synchronized关键字一定会保证实现，即使考虑到工作内存的这一方面
    private Status status;

    private AtomicInteger status0 = new AtomicInteger(0); //原子类没必要加volatile
    private final static Integer RUNNING = 1;
    private final static Integer STOP = 0;
    /**
     * 若干参数 核心线程数，最大线程数，存活时间，时间单元，线程工厂，阻塞队列，丢弃策略
     */
    //不允许修改的，所以说没有必要用volatile关键字
    private Long coreThreadNum;

    private Long maxThreadNum;

    private Long keepAliveTime;

    private TimeUnit unit;

    //BlockingQueue本身就是线程安全的，所以说和加了锁的变量是一样的
    private BlockingQueue<Runnable> workQueue;

    //默认的线程工厂十分简单
    private SimpleThreadFactory tf;

    //丢弃策略暂不实现

    /**
     * 辅助参数 Worker集合 当前核心线程数
     */
    protected Set<Worker> workers = new HashSet<>();

    protected AtomicInteger currentThreadNum = new AtomicInteger(0);

    /**
     * 执行线程的锁: 声明为全局变量才更加合理，如果是局部变量的话，其实是没有办法加锁的
     */

    ReentrantLock lock = new ReentrantLock();
    /**
     * 构造方法还没写
     */
    public SimpleThreadPoolExecutor(long coreThreadNum,
                                    long maxThreadNum,
                                    long keepAliveTime,
                                    TimeUnit unit,
                                    BlockingQueue<Runnable> workQueue
                                    ) {
        this.coreThreadNum = coreThreadNum;
        this.maxThreadNum = maxThreadNum;
        this.keepAliveTime = keepAliveTime;
        this.unit = unit;
        this.workQueue = workQueue;
        tf = new SimpleThreadFactory();
        setStatus(1);
    }

    /**
     * 设置线程池的状态
     */
    public synchronized void setStatus(Integer f) {
        this.status = f == 1 ? Status.RUNNING : Status.STOP;
        status0.set(f == 1 ? RUNNING : STOP);
    }

    /**
     * 判断线程池的状态
     * true表示正在运行 false表示停止运行
     * @return 布尔值
     */
    public synchronized boolean getStatus() {
        return this.status == Status.RUNNING && this.status0.get() == RUNNING;
    }

    @Override
    public  void execute(Runnable command) {
        if(command == null)
            throw new NullPointerException();
        /**
         * 经典三种情况
         */
        //这里的synchronized同时考虑和shutdown的冲突
        //如果用synchronized的话下面的方法都会被锁住，所以是不可以的
        //所以说这里的并发控制只需要改一部分get/set方法就可以
        //如果正在运行的话

        if(getStatus()) {

            if(currentThreadNum.get() < coreThreadNum) {
                addWorker(command, true);
                //这一步提取到前面不太合适，因为会造成万一没有成功的假象
                //currentThreadNum.incrementAndGet();
            }
            /**
             * 添加成功就添加，因为阻塞队列是作为参数传入的
             * 另外，如果当前任务过多的话，直接采取抛弃的策略
             */
            if(!workQueue.offer(command)) {
                if(currentThreadNum.get() < maxThreadNum) {
                    addWorker(command, false);
                    //currentThreadNum.incrementAndGet();
                }
            }
        }
    }

    /**
     *关闭线程池
     */
    public synchronized void shutDown() {
        setStatus(0);
        interruptAllWorkers();
    }

    /**
     * 释放所有工作线程
     */
    private void interruptAllWorkers() {
        lock.lock();
        try {
            for (Worker worker : workers) {
                if(!worker.t.isInterrupted()){
                    worker.t.interrupt();
                }
            }
        } finally {
            lock.unlock();
        }
    }
    /**
     *关键是实现内部类Worker
     */
    protected class Worker implements Runnable {
        /**
         * 自身的线程
         */
        private Thread t;

        private final Runnable firstTask;

        public Worker(Runnable firstTask) {
            this.firstTask = firstTask;
            t = tf.getInstanceOfThread(this);
        }

        @Override
        public void run() {
            runWorker(this);
        }
    }

    /**
     * 运行对应的工作线程
     * @param w: 对应的要运行的线程
     */
    public void runWorker(Worker w) {
        if(w == null)
            throw new NullPointerException();

        Runnable r = w.firstTask;
        try {
            while(r != null || (r = getTask()) != null) {
                if(!getStatus())
                        throw new InterruptedException();
                r.run();
                r = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //processWorkerExit()
        lock.lock();
        workers.remove(w);
        lock.unlock();
    }
    //这里可能需要阻塞线程的事情
    public Runnable getTask() {
        //实现的思路是通过实时的线程数来实现的
        if(!getStatus())
            return null;
        try {
            return (currentThreadNum.get() > coreThreadNum) ?
                    workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
                    workQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 添加工作线程
     * @param r:一旦添加好工作线程之后，实际上线程就已经开始运行了
     */
    public void addWorker(Runnable r, boolean f) {
        //和之前是一样的，重复判断

        retry:
        while (true){
            if(!getStatus()){
                return;
            }
            while (true){
                // 想创建工作线程，但是已经 创建完了
                if(f && currentThreadNum.get() >= coreThreadNum)
                    return;
                if(!f && currentThreadNum.get() >= maxThreadNum)
                    //reject策略
                    return;
                // 到这里，说明我们可以创建一个工作线程了
                if(!add()){
                    // 自增失败，可能是因为高并发，导致的原子操作失败了
                    continue retry;
                }
                // 到这里，说明我们可以创建一个工作线程了
                // 说明自增成功了,跳出双层循环
                break retry;
            }
        }

        Worker w = new Worker(r);
        Thread t = w.t;

        lock.lock();
        workers.add(w);
        lock.unlock();

        //显然就不能加锁
        t.start();
    }

    public boolean add() {
        return currentThreadNum.compareAndSet(currentThreadNum.get(), currentThreadNum.get() + 1);
    }

    public boolean minus() {
        return currentThreadNum.compareAndSet(currentThreadNum.get(), currentThreadNum.get() - 1);
    }
}
