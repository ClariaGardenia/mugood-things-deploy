package com.muGood.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.muGood")
@MapperScan("com.muGood.infrastructure.mapper")
public class MuGoodApplication {

    public static void main(String[] args) {
        SpringApplication.run(MuGoodApplication.class, args);
        System.out.println("运行成功！！！");
    }
}
