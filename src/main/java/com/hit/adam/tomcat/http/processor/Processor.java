package com.hit.adam.tomcat.http.processor;

import com.hit.adam.tomcat.http.HttpRequest;
import com.hit.adam.tomcat.http.HttpResponse;

import java.io.IOException;

public interface Processor {
    void process(HttpRequest request, HttpResponse response) throws IOException;
}
