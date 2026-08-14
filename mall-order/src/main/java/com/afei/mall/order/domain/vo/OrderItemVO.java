package com.afei.mall.order.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemVO {
    private Long skuId;      // SKU ID
    private String title;    // 商品标题（下单快照）
    private String image;    // 商品图片（下单快照）
    private Long price;      // 单价（分，下单快照）
    private Integer num;     // 购买数量
}
