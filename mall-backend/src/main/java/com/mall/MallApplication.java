package com.mall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 商城系统启动类
 */
@SpringBootApplication
@MapperScan("com.mall.mapper")
public class MallApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallApplication.class, args);
        System.out.println("===========================================");
        System.out.println("商城系统启动成功！");
        System.out.println("===========================================");
    }
}

