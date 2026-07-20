package com.afei.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("阿飞商城 API 文档")
                        .description("基于 Spring Cloud Alibaba 的微服务电商系统")
                        .version("1.0.0")
                        .contact(new Contact().name("阿飞")));
    }
}
