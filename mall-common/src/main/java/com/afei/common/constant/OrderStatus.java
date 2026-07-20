package com.afei.common.constant;

import lombok.Getter;

@Getter
public enum OrderStatus {

    WAIT_PAY(0, "待付款"),
    PAID(1, "已付款"),
    SHIPPED(2, "已发货"),
    RECEIVED(3, "已签收"),
    CANCELED(4, "已取消"),
    REFUNDING(5, "退款中"),
    REFUNDED(6, "已退款");

    private final Integer code;
    private final String desc;

    OrderStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OrderStatus getByCode(Integer code) {
        for (OrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
