package com.atguigu.gmall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * projectName: GmallTwicePrastice
 * 吾常终日而思矣 不如须臾之所学!
 *
 * @author: Clarence
 * time: 2023/4/8 23:24 周六
 * description:
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableFeignClients(basePackages = "com.atguigu.gmall")
@EnableDiscoveryClient
public class ServiceCartApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceCartApplication.class,args);
    }
}
