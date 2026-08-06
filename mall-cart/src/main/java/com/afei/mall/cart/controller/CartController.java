package com.afei.mall.cart.controller;

import com.afei.common.exception.BusinessException;
import com.afei.common.result.Result;
import com.afei.mall.cart.domain.dto.CartItemSaveDTO;
import com.afei.mall.cart.domain.dto.CartMergeDTO;
import com.afei.mall.cart.domain.dto.CartNumDTO;
import com.afei.mall.cart.domain.dto.CartSelectDTO;
import com.afei.mall.cart.domain.vo.CartVO;
import com.afei.mall.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "购物车接口")
@AllArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/list")
    @Operation(summary = "购物车列表")
    public Result<CartVO> getList(@RequestHeader("Authorization") String authorization) {
        String token = extractToken(authorization);
        return Result.success(cartService.getList(token));
    }

    @PostMapping
    @Operation(summary = "添加购物车")
    public Result<Void> add(@RequestHeader("Authorization") String authorization,
                            @RequestBody @Valid CartItemSaveDTO dto) {
        cartService.add(dto, extractToken(authorization));
        return Result.success();
    }
    @PutMapping("/{skuId}")
    @Operation(summary = "修改数量")
    public Result<Void> update(@RequestHeader("Authorization") String authorization,
                               @PathVariable Long skuId,
                               @RequestBody @Valid CartNumDTO dto) {
        cartService.updateNum(skuId, dto, extractToken(authorization));
        return Result.success();
    }
    @DeleteMapping("/{skuId}")
    @Operation(summary = "删除购物车")
    public Result<Void> delete(@RequestHeader("Authorization") String authorization,
                               @PathVariable Long skuId) {
        cartService.delete(skuId, extractToken(authorization));
        return Result.success();
    }

    @PutMapping("/select/{skuId}")
    @Operation(summary = "选中/取消选中")
    public Result<Void> select(@RequestHeader("Authorization") String authorization,
                               @PathVariable Long skuId,
                               @RequestBody @Valid CartSelectDTO dto) {
        cartService.select(skuId, dto.getSelected(), extractToken(authorization));
        return Result.success();
    }
    @PutMapping("/selectAll")
    @Operation(summary = "全选/取消全选")
    public Result<Void> selectAll(@RequestHeader("Authorization") String authorization,
                                  @RequestBody @Valid CartSelectDTO dto) {
        cartService.selectAll(dto.getSelected(), extractToken(authorization));
        return Result.success();
    }

    @GetMapping("/count")
    @Operation(summary = "购物车数量")
    public Result<Integer> getCount(@RequestHeader("Authorization") String authorization) {
        String token = extractToken(authorization);
        return Result.success(cartService.getCount(token));
    }

    @PostMapping("/merge")
    @Operation(summary = "合并购物车")
    public Result<Void> mergeCart(@RequestHeader("Authorization") String authorization,
                                  @RequestBody @Valid CartMergeDTO dto) {
        cartService.mergeCart(dto, extractToken(authorization));
        return Result.success();
    }


    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException("未登录");
        }
        return authHeader.substring(7);
    }
}
