package com.afei.mall.pay.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayStatusVO {
    private String payNo;           // 支付流水号
    private Long orderId;           // 订单 ID
    private Long amount;            // 支付金额（分）
    private Integer payStatus;      // 1=未支付 2=已支付 3=已退款
    private String payStatusText;   // 状态文字
    private LocalDateTime payTime;  // 支付时间
}