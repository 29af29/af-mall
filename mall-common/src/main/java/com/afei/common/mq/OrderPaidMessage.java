package com.afei.common.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaidMessage implements Serializable {

    private String orderNo;       // 订单号
    private String transactionId; // 支付交易号
}
