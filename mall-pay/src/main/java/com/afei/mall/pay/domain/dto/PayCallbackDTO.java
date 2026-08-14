package com.afei.mall.pay.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayCallbackDTO {

    @NotBlank(message = "支付流水号不能为空")
    private String payNo;     // 支付流水号（发起支付返回的）

    @NotBlank(message = "第三方交易号不能为空")
    private String tradeNo;   // 第三方支付交易号

    @NotBlank(message = "支付状态不能为空")
    private String status;    // SUCCESS / REFUND / NOTPAY / CLOSED

    private String sign;      // 签名（验签用）
}
