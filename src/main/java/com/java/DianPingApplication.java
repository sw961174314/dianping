package com.java;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.java.mapper")
@SpringBootApplication
public class DianPingApplication {

    public static void main(String[] args) {
        SpringApplication.run(DianPingApplication.class, args);
    }

}
