package com.afei.mall.product.controller;

import com.afei.common.result.Result;
import com.afei.mall.product.domain.dto.StockDTO;
import com.afei.mall.product.service.SpuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品内部接口：仅服务间 Feign 调用（/internal/** 不在网关路由中，外部无法访问）
 * 使用场景：订单模块扣减 / 回补 SKU 库存（配合 Seata AT 分布式事务）
 */
@RestController
@RequestMapping("/internal/product")
@Tag(name = "商品内部接口")
@AllArgsConstructor
public class InternalProductController {

    private final SpuService spuService;

    @PutMapping("/sku/{id}/stock")
    @Operation(summary = "扣减库存（内部）")
    public Result<Void> deductStock(@PathVariable Long id,
                                    @RequestBody @Valid StockDTO dto) {
        spuService.deductStock(id, dto);
        return Result.success();
    }

    @PutMapping("/sku/{id}/stock/restore")
    @Operation(summary = "恢复库存（内部）")
    public Result<Void> restoreStock(@PathVariable Long id,
                                     @RequestBody @Valid StockDTO dto) {
        spuService.restoreStock(id, dto);
        return Result.success();
    }
}
