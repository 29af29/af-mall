package com.afei.mall.order.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateVO {
    private Long orderId;           // 订单 ID
    private String orderNo;          // 订单号
    private Long totalPay;           // 应付金额（分）
    private Long actualPay;          // 实付金额（分）
    private Integer status;          // 订单状态
    private LocalDateTime createTime; // 下单时间
}