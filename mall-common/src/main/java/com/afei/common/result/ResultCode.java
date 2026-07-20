package com.afei.common.result;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),

    //参数错误 400xx
    PARAM_ERROR(40001, "参数校验失败"),
    PARAM_MISSING(40002, "参数缺失"),

    // 认证错误 401xx
    UNAUTHORIZED(40101, "Token无效"),
    TOKEN_EXPIRED(40102, "Token已过期"),
    NOT_LOGGED_IN(40103, "未登录"),

    // 权限错误 403xx
    FORBIDDEN(40301, "权限不足"),

    // 资源不存在 404xx
    NOT_FOUND(40401, "资源不存在"),

    // 业务错误 500xx
    BUSINESS_ERROR(50001, "业务异常");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
