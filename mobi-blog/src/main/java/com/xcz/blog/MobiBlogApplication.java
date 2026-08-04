package com.xcz.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Mobi Blog 博客服务启动类
 */
@EnableAsync
@EnableScheduling
@EnableDiscoveryClient
@SpringBootApplication
public class MobiBlogApplication {

    /**
     * 应用入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(MobiBlogApplication.class, args);
    }
}
