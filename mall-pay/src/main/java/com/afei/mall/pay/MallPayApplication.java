package com.afei.mall.pay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.afei.common.feign")
@SpringBootApplication(scanBasePackages = "com.afei")
public class MallPayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallPayApplication.class, args);
    }
}
