package com.afei.common.config;

import com.afei.common.interceptor.InternalApiInterceptor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.TimeZone;

/**
 * Web 全局配置：跨域 + 内部接口鉴权拦截器 + Jackson 序列化
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final InternalApiInterceptor internalApiInterceptor;

    public WebConfig(InternalApiInterceptor internalApiInterceptor) {
        this.internalApiInterceptor = internalApiInterceptor;
    }

    /**
     * 注册内部接口鉴权拦截器：/internal/** 仅限携带 X-Internal-Token 的服务间调用
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(internalApiInterceptor)
                .addPathPatterns("/internal/**");
    }

    /**
     * CORS 跨域配置
     * 开发阶段允许所有来源，上线后改为具体域名
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * Jackson 序列化配置
     * 1. 日期序列化为 ISO 字符串（如 2026-07-17T15:00:00）而非时间戳
     * 2. 反序列化时未知字段不报错
     * 3. 时区设为东八区
     * 4. Long 类型全局序列化为 String：解决 MyBatis-Plus 雪花 ID（19位）传给前端 JS Number 精度丢失
     *    前端拿到字符串直接拼接/展示；Spring MVC 接收 Long 字段会用 Long.parseLong() 正确还原
     *    金额字段也是 Long（分），但前端只展示不做算术，无影响
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            builder.modules(new JavaTimeModule());
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            builder.timeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            // Long 类型全局序列化为 String（覆盖所有 VO 的 id/userId/skuId/spuId 等字段）
            builder.serializerByType(Long.class, ToStringSerializer.instance);
        };
    }
}
