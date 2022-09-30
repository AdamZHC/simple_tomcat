package com.hit.adam.tomcat.connector.threadpool;

public enum Status {
    RUNNING("运行中", 1), STOP("停止", 0);
    private String description;
    private Integer code;
    Status(String description, Integer code) {
        this.description = description;
        this.code = code;
    }
}
