package com.afei.mall.cart.service;

import com.afei.mall.cart.domain.dto.CartItemSaveDTO;
import com.afei.mall.cart.domain.dto.CartMergeDTO;
import com.afei.mall.cart.domain.dto.CartNumDTO;
import com.afei.mall.cart.domain.vo.CartVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface CartService{
    CartVO getList(String token);

    void add(CartItemSaveDTO dto, String token);

    void updateNum(Long skuId, CartNumDTO dto, String token);

    void delete(Long skuId, String token);

    void select(Long skuId, Boolean selected, String token);

    void selectAll(@NotNull(message = "选中状态不能为空") Boolean selected, String token);

    Integer getCount(String token);

    void mergeCart(CartMergeDTO dto, String token);
}
