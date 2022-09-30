package com.hit.adam.tomcat.connector.impl;

import com.hit.adam.tomcat.connector.Connector;
import com.hit.adam.tomcat.connector.threadpool.SimpleThreadPoolExecutor;
import com.hit.adam.tomcat.processor.impl.NioHttpProcessor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;

@SuppressWarnings("All")
public class NioHttpConnector implements Connector {

    private final int BUF_SIZE = 1 << 10;
    private final int PORT = 8080;
    private final int TIMEOUT = 3000;
    /**
     * 首先声明多路复用器Selector
     * 监听套接字ServerSocketChannel
     */
    private Selector selector;
    private ServerSocketChannel ssc;
    /**
     * map待使用，添加后续功能
     */
    private Executor executor;

    public void start() {
        new Thread(this).start();
    }

    @Override
    public void initialize() {
        executor = Executors.newFixedThreadPool(20);
//        executor = new SimpleThreadPoolExecutor(10, 30,20, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(15));
    }

    @Override
    public void run() {
        try {
            /**
             * 工厂模式获取selector实例
             * 绑定端口和设置阻塞方式的前期工作
             * 根据类型注册
             */
            selector = Selector.open();
            ssc = ServerSocketChannel.open();
            ssc.socket().bind(new InetSocketAddress(PORT));
            ssc.configureBlocking(false);
            /**
             * register参数很多，还可以附加对象
             */
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {

                if (selector.selectNow() == 0) {
                    continue;
                }
                /**
                 * selector本身就被维护着
                 * 或者说即使视图移除了SelectionKey
                 * 但是实际上也会由于就绪还能获取到
                 */
                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                /**
                 * 有线程的问题，要直接把他移除掉
                 */
                while (iter.hasNext()) {

                    SelectionKey key = iter.next();

                    /**
                     * 这里就是不断获取socket并且注册给多路复用器selector
                     * 最后再处理后续的读还有写的问题
                     */
                    if (key.isAcceptable()) {
                        /**
                         * 因为监听套接字也在这个迭代器里面
                         * 所以可以获取处理这个，因为这个监听套接字触发了事件
                         * key就是代表socket的监听情况，如果被触发了那就实现处理动作
                         * 并且key里面有所有的参数
                         */
                        handleAccept(key);
                        iter.remove();
                    } else if (key.isReadable() && key.isValid()) {
                        handleRead(key);
                        /**
                         * 这里key直接remove
                         * 那边的key直接处理，其实是不会扔掉的
                         * 只不过是在这里直接扔到canceled-pool中
                         * 然后下一次不会把他搜出来
                         */
                        key.cancel();
                        iter.remove();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (selector != null) {
                    selector.close();
                }
                if (ssc != null) {
                    ssc.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel ssChannel = (ServerSocketChannel) key.channel();
        SocketChannel sc = ssChannel.accept();
        sc.configureBlocking(false);
        sc.register(key.selector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE, ByteBuffer.allocateDirect(BUF_SIZE));
    }

    public void handleRead(SelectionKey key) throws IOException {
        executor.execute(new NioHttpProcessor(this, key));
    }

}
