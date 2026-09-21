package com.afei.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 内部接口访问控制（服务间鉴权）
 * <p>
 * 只拦截 /internal/** 路径。该路径不在网关路由中，仅服务间 Feign 调用可达；
 * 这里再校验 X-Internal-Token，防止绕过网关直连服务端口调用内部接口。
 */
@Slf4j
@Component
public class InternalApiInterceptor implements HandlerInterceptor {

    @Value("${internal.token:afei-mall-internal-token-2024}")
    private String internalToken;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("X-Internal-Token");
        if (internalToken.equals(token)) {
            return true;
        }
        log.warn("内部接口被非法访问: {} {}, token={}", request.getMethod(), request.getRequestURI(), token);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":40301,\"message\":\"禁止访问内部接口\",\"data\":null}");
        return false;
    }
}
