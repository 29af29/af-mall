package com.afei.mall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.afei.common.feign")
@SpringBootApplication(scanBasePackages = {"com.afei.common", "com.afei.mall.cart"}, exclude = DataSourceAutoConfiguration.class)
public class MallCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallCartApplication.class, args);
    }
}
