package com.afei.mall.notify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.afei.common", "com.afei.mall.notify"})
public class MallNotifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallNotifyApplication.class, args);
    }

}
