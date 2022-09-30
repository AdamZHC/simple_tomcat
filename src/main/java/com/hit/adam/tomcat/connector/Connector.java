package com.hit.adam.tomcat.connector;

/**
 * 根据tomcat4连接器接口实现一个自己的类型
 */
public interface Connector extends Runnable{
    /**
     * 考虑到Container单纯实现对于Servlet的调用，因此不会提取出来
     * 后面可能考虑功能完善会添加一个Container
     */
    void start();
    void initialize();
}
