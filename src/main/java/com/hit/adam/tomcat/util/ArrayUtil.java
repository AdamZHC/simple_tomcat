package com.hit.adam.tomcat.util;

public class ArrayUtil {
    private final static int BASE_BUFFER_SIZE =  1 << 8;
    private final static int MULTI_BUFFER_SIZE = 1 << 2;

    /**
     * 获取缓冲数组
     * @return 缓冲byte[]数组
     */
    public static byte[] getBufferByteArray() {
        return new byte[BASE_BUFFER_SIZE * MULTI_BUFFER_SIZE];
    }

    /**
     * 获取缓冲区大小
     * @return 对应的缓冲区大小
     */
    public static int getBufferSize() {
        return BASE_BUFFER_SIZE * MULTI_BUFFER_SIZE;
    }
}
