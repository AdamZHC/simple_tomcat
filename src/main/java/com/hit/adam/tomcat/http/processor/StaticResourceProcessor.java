package com.hit.adam.tomcat.http.processor;

import com.hit.adam.tomcat.http.HttpRequest;
import com.hit.adam.tomcat.http.HttpResponse;

import java.io.IOException;

public class StaticResourceProcessor implements Processor{
    @Override
    /**
     * 不能给用户使用，但是同时不能设置为private，因为其它的类也要调用该方法
     */
    public void process(HttpRequest request, HttpResponse response) throws IOException {
        /**
         * 这里根据空值来判断就可以
         */
        if(response.getChannel() == null)
            response.bioSendStaticResouce();
        else
            response.nioSendStaticResouce();

    }
}
