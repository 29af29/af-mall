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
public class OrderPageVO {
    private Long id;                // 订单 ID
    private String orderNo;         // 订单号
    private Long totalAmount;       // 总金额（分）
    private Long payAmount;         // 实付金额（分）
    private Integer status;         // 订单状态
    private String goodsTitle;      // 首个商品标题（列表展示用）
    private String goodsPic;        // 首个商品图片（列表展示用）
    private Integer goodsNum;       // 商品总数量
    private LocalDateTime createTime; // 下单时间
}
