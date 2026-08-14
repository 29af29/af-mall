package com.afei.mall.cart.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartVO {
    private List<CartItemVO> items;   // 购物车商品列表
    private Long totalPrice;          // 总价（分，仅计算选中商品）
    private Integer totalCount;       // 商品种类数
}
