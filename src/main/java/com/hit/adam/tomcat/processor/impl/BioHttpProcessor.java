package com.hit.adam.tomcat.processor.impl;

import com.hit.adam.tomcat.connector.Connector;
import com.hit.adam.tomcat.http.HttpRequest;
import com.hit.adam.tomcat.http.HttpResponse;
import com.hit.adam.tomcat.processor.Processor;
import com.hit.adam.tomcat.http.processor.ServletProcessor;
import com.hit.adam.tomcat.http.processor.StaticResourceProcessor;
import com.hit.adam.tomcat.util.ServerUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@SuppressWarnings("All")
public class BioHttpProcessor implements Processor {
    private HttpRequest request;
    private HttpResponse response;
    private Connector connector;
    private Socket socket;

    public BioHttpProcessor(Connector connector, Socket socket) {
        this.connector = connector;
        this.socket = socket;
    }

    public void process() {
        try {
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();
            //初始化request以及response 解析request请求行和请求头
            request = new HttpRequest(input);
            response = new HttpResponse(output, request);
            //调用对应的处理器处理
            System.out.println("Process request...");
            System.out.println(Thread.currentThread());
            if (request.getPath() != null && request.getPath().startsWith(ServerUtil.SERVLET_ROOT)) {
                new ServletProcessor().process(request, response);
            } else {
                new StaticResourceProcessor().process(request, response);
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        process();
    }
}
