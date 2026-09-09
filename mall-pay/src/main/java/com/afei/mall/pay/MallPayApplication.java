package com.afei.mall.pay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients(basePackages = "com.afei.common.feign")
@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.afei")
public class MallPayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallPayApplication.class, args);
    }
}
