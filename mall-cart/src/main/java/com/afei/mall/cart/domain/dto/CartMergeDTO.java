package com.afei.mall.cart.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CartMergeDTO {
    @NotNull(message = "合并项不能为空")
    private List<CartItemSaveDTO> items;  // 游客购物车中要合并的商品
}
