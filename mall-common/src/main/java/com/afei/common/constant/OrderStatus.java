package com.afei.common.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OrderStatus {

    WAIT_PAY(1, "待付款"),
    PAID(2, "已付款"),
    SHIPPED(3, "已发货"),
    RECEIVED(4, "已签收"),
    CANCELED(5, "已取消"),
    REFUNDING(6, "退款中"),
    REFUNDED(7, "已退款");

    private final Integer code;
    private final String desc;

    OrderStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonValue
    public StatusVO toVO() {
        return new StatusVO(code, desc);
    }

    public static OrderStatus getByCode(Integer code) {
        for (OrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    @Getter
    public static class StatusVO {
        private final Integer code;
        private final String label;

        public StatusVO(Integer code, String label) {
            this.code = code;
            this.label = label;
        }
    }
}
