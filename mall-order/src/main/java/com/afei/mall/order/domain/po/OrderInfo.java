package com.afei.mall.order.domain.po;

import com.afei.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_info")
public class OrderInfo extends BaseEntity {

    private String orderNo;            // 订单号
    private Long userId;               // 用户 ID
    private Long totalAmount;          // 总金额（分）
    private Long payAmount;            // 实付金额（分）
    private Long freightAmount;        // 运费（分）
    private Integer status;            // 1=待付款 2=已付款 3=已发货 4=已签收 5=已取消 6=退款中 7=已退款
    private Integer payType;           // 支付方式：1=微信 2=支付宝
    private String receiverName;       // 收货人
    private String receiverPhone;      // 收货手机号
    private String receiverAddress;    // 收货地址
    private String remark;             // 订单备注
    private LocalDateTime paymentTime; // 支付时间
    private LocalDateTime deliveryTime;// 发货时间
    private LocalDateTime receiveTime; // 签收时间
    private LocalDateTime closeTime;   // 关闭时间
}
