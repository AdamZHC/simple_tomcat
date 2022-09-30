package com.hit.adam.tomcat.connector.impl;

import com.hit.adam.tomcat.connector.Connector;
import com.hit.adam.tomcat.processor.impl.BioHttpProcessor;
import com.hit.adam.tomcat.connector.threadpool.SimpleThreadPoolExecutor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("All")
public class BioHttpConnector implements Connector {

    private Executor executor;

    private ServerSocket serverSocket;

    @Override
    public void start() {
        new Thread(this).start();
    }

    @Override
    public void initialize() {
        executor = new SimpleThreadPoolExecutor(10, 30,20, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(15));
    }

    @Override
    public void run() {
        try {
            serverSocket = getServerSocket();
            while (true) {
                Socket accept = serverSocket.accept();
                /**
                 * 获取客户端线程
                 */
                System.out.println("Gotten socket...");
                BioHttpProcessor processor = new BioHttpProcessor(this, accept);
                /**
                 * 每次完成http的处理之后就会释放该资源，所以说也是可以的
                 */
                executor.execute(processor);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ServerSocket getServerSocket() throws IOException {
        return new ServerSocket(8080, 1, InetAddress.getByName("127.0.0.1"));
    }
}
