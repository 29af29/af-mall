package com.afei.mall.pay.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("payment_info")
public class PaymentInfo {

    @TableId(type = IdType.AUTO)
    private Long id;                    // 主键
    private String orderNo;             // 订单号
    private String transactionId;       // 第三方交易号（假支付用 UUID 代替）
    private Integer paymentType;        // 1=微信 2=支付宝
    private Long totalAmount;           // 支付金额（分）
    private String tradeState;          // NOTPAY / SUCCESS
    private String callbackContent;     // 回调原始 JSON
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime paymentTime;  // 支付时间
    private LocalDateTime updateTime;   // 更新时间
}
