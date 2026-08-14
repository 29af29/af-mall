package com.afei.mall.order.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailVO {
    private Long id;                  // 订单 ID
    private String orderNo;           // 订单号（雪花算法）
    private Long totalAmount;         // 总金额（分）
    private Long payAmount;           // 实付金额（分）
    private Long freightAmount;       // 运费（分）
    private Integer status;           // 订单状态：1=待付款 2=已付款 3=已发货 4=已签收 5=已关闭 6=退款中 7=已退款
    private Integer payType;          // 支付方式：1=微信 2=支付宝
    private String receiverName;      // 收货人
    private String receiverPhone;     // 收货手机号
    private String receiverAddress;   // 收货地址
    private String remark;            // 订单备注
    private LocalDateTime paymentTime; // 支付时间
    private LocalDateTime deliveryTime;// 发货时间
    private LocalDateTime receiveTime; // 收货时间
    private LocalDateTime closeTime;   // 关闭时间
    private LocalDateTime createTime;  // 创建时间
    private List<OrderItemVO> orderItems; // 订单商品明细
}
