package com.afei.common.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单超时消息：下单后延迟发送，用于检查未支付订单并自动关单
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTimeoutMessage implements Serializable {

    private Long orderId;
    private String orderNo;
}
