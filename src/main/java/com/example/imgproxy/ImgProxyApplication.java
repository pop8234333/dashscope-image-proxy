package com.example.imgproxy;

import com.example.imgproxy.config.ProxyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ProxyProperties.class)
public class ImgProxyApplication {
    public static void main(String[] args) {
        // Spring Boot 启动入口
        SpringApplication.run(ImgProxyApplication.class, args);
    }
}
