package com.afei.mall.order.controller;

import com.afei.common.result.Result;
import com.afei.mall.order.domain.dto.StatusSaveDTO;
import com.afei.mall.order.service.OrderService;
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
 * 订单内部接口：仅服务间 Feign 调用（/internal/** 不在网关路由中，外部无法访问）
 * 使用场景：支付模块支付成功后修改订单状态
 */
@RestController
@RequestMapping("/internal/order")
@Tag(name = "订单内部接口")
@AllArgsConstructor
public class InternalOrderController {

    private final OrderService orderService;

    @PutMapping("/{id}/status")
    @Operation(summary = "修改订单状态（内部）")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestBody @Valid StatusSaveDTO dto) {
        orderService.updateStatus(id, dto);
        return Result.success();
    }

    @PutMapping("/no/{orderNo}/status")
    @Operation(summary = "按订单号修改状态（内部）")
    public Result<Void> updateStatusByOrderNo(@PathVariable String orderNo,
                                              @RequestBody @Valid StatusSaveDTO dto) {
        orderService.updateStatusByOrderNo(orderNo, dto);
        return Result.success();
    }
}
