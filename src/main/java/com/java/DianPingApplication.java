package com.java;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.java.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class DianPingApplication {
    public static void main(String[] args) {
        SpringApplication.run(DianPingApplication.class, args);
    }
}