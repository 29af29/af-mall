package com.afei.mall.cart.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemVO {
    private Long skuId;        // SKU ID
    private String title;      // SKU 标题
    private String image;      // SKU 图片
    private Long price;        // 单价（分）
    private Integer num;       // 购买数量
    private Integer stock;     // 库存数量
    private Boolean selected;  // 是否选中
}
