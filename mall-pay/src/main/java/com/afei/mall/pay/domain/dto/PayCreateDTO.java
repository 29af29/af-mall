package com.afei.mall.pay.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayCreateDTO {

    @NotNull(message = "订单 ID 不能为空")
    private Long orderId;    // 订单 ID

    @NotNull(message = "支付方式不能为空")
    @Min(value = 1, message = "支付方式无效")
    @Max(value = 2, message = "支付方式无效")
    private Integer payType; // 1=微信 2=支付宝
}
