package com.afei.mall.cart.service;

import com.afei.mall.cart.domain.dto.CartItemSaveDTO;
import com.afei.mall.cart.domain.dto.CartMergeDTO;
import com.afei.mall.cart.domain.dto.CartNumDTO;
import com.afei.mall.cart.domain.vo.CartVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface CartService{
    CartVO getList(Long userId);

    void add(CartItemSaveDTO dto, Long userId);

    void updateNum(Long skuId, CartNumDTO dto, Long userId);

    void delete(Long skuId, Long userId);

    void select(Long skuId, Boolean selected, Long userId);

    void selectAll(@NotNull(message = "选中状态不能为空") Boolean selected, Long userId);

    Integer getCount(Long userId);

    void mergeCart(CartMergeDTO dto, Long userId);
}
