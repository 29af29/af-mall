package com.afei.common.feign;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;

/**
 * Feign 全局拦截器
 * <p>
 * 1. 无条件携带 X-Internal-Token：服务间调用内部接口（/internal/**）的认证凭证；
 *    MQ 消费者 / 定时任务触发的 Feign 调用没有 HTTP 请求上下文，也必须带上该头。
 * 2. 把当前线程绑定的 HTTP 请求头 X-User-Id/X-User-Role 透传到下游服务，
 *    解决：网关已写入请求头但 OpenFeign 默认不透传的问题。
 *
 * @author afei
 */
@Configuration
public class FeignConfig {

    @Value("${internal.token:afei-mall-internal-token-2024}")
    private String internalToken;

    @Bean
    public RequestInterceptor headerRelayInterceptor() {
        return template -> {
            // 内部接口服务间认证：所有 Feign 调用统一携带
            template.header("X-Internal-Token", internalToken);

            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                // 没有绑定的 HTTP 请求（如定时任务 / MQ 消费者触发的 Feign 调用）
                return;
            }
            HttpServletRequest request = attrs.getRequest();
            Enumeration<String> names = request.getHeaderNames();
            if (names == null) {
                return;
            }
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                // 只透传认证相关的内部 header，避免把 Cookie 等无关头带到下游
                if (name.equalsIgnoreCase("X-User-Id")
                        || name.equalsIgnoreCase("X-User-Role")) {
                    String value = request.getHeader(name);
                    if (value != null && !value.isEmpty()) {
                        template.header(name, value);
                    }
                }
            }
        };
    }
}
