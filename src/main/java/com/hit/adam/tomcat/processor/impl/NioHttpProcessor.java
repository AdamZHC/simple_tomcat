package com.hit.adam.tomcat.processor.impl;

import com.hit.adam.tomcat.connector.Connector;
import com.hit.adam.tomcat.http.HttpRequest;
import com.hit.adam.tomcat.http.HttpResponse;
import com.hit.adam.tomcat.processor.Processor;
import com.hit.adam.tomcat.http.processor.ServletProcessor;
import com.hit.adam.tomcat.http.processor.StaticResourceProcessor;
import com.hit.adam.tomcat.util.ServerUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

@SuppressWarnings("All")
public class NioHttpProcessor implements Processor {
    private HttpRequest request;
    private HttpResponse response;
    private Connector connector;
    private SelectionKey selectionKey;

    public NioHttpProcessor(Connector connector, SelectionKey selectionKey) {
        this.connector = connector;
        this.selectionKey = selectionKey;
    }

    public void process() {
        try {
            SocketChannel sc = (SocketChannel) selectionKey.channel();
            /**
             * 使用key获取到的buffer也就是满足了聚合一致性的
             */
            ByteBuffer buf = (ByteBuffer) selectionKey.attachment();
            //selectionKey.cancel();
            //初始化request以及response 解析request请求行和请求头
            request = new HttpRequest(sc, buf);
            response = new HttpResponse(sc, request);
            //调用对应的处理器处理
            System.out.println("Nio Process request...");
            System.out.println(Thread.currentThread());
            if (request.getPath() != null && request.getPath().startsWith(ServerUtil.SERVLET_ROOT)) {
                new ServletProcessor().process(request, response);
            } else {
                new StaticResourceProcessor().process(request, response);
            }
            //需要把数据写回才能回收这个key
            selectionKey.cancel();
            sc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        process();
    }
}
