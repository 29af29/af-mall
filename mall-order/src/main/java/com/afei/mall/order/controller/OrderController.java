package com.afei.mall.order.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.afei.common.constant.OrderStatus;
import com.afei.common.exception.BusinessException;
import com.afei.common.result.PageResult;
import com.afei.common.result.Result;
import com.afei.mall.order.domain.dto.OrderCreateDTO;
import com.afei.mall.order.domain.dto.OrderPageQueryDTO;
import com.afei.mall.order.domain.dto.StatusSaveDTO;
import com.afei.mall.order.domain.vo.OrderCreateVO;
import com.afei.mall.order.domain.vo.OrderDetailVO;
import com.afei.mall.order.domain.vo.OrderPageVO;
import com.afei.mall.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@Tag(name = "订单接口")
@AllArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "创建订单")
    @SentinelResource(value = "createOrder", blockHandler = "createOrderBlockHandler")
    public Result<OrderCreateVO> createOrder(@RequestHeader("authorization") String authorization,
                                             @RequestBody @Valid OrderCreateDTO orderCreateDTO) {
        return Result.success(orderService.createOrder(extractToken(authorization), orderCreateDTO));
    }

    /**
     * 限流/熔断降级处理：返回友好提示，而不是默认的 429
     */
    public static Result<OrderCreateVO> createOrderBlockHandler(String authorization,
                                                                OrderCreateDTO orderCreateDTO,
                                                                BlockException e) {
        return Result.error(429, "系统繁忙，请稍后再试");
    }

    @GetMapping("/page")
    @Operation(summary = "订单分页")
    public Result<PageResult<OrderPageVO>> orderPage(@RequestHeader("authorization") String authorization,
                                                     OrderPageQueryDTO orderPageQueryDTO){
        return Result.success(orderService.orderPage(extractToken(authorization), orderPageQueryDTO));
    }
    @GetMapping("/{id}")
    @Operation(summary = "订单详情")
    public Result<OrderDetailVO> orderDetail(@RequestHeader("authorization") String authorization,
                                            @PathVariable Long id){
        return Result.success(orderService.orderDetail(extractToken(authorization), id));
    }
    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单")
    public Result<Void> cancelOrder(@RequestHeader("authorization") String authorization,
                                    @PathVariable Long id){
        orderService.cancelOrder(extractToken(authorization), id);
        return Result.success();
    }
    @GetMapping("/status/list")
    @Operation(summary = "订单状态枚举")
    public Result<OrderStatus[]> orderStatusList(@RequestHeader("authorization") String authorization){
        extractToken(authorization);
        return Result.success(OrderStatus.values());
    }


    @PutMapping("/{id}/status")
    @Operation(summary = "修改订单状态（服务间调用，不校验用户）")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestBody @Valid StatusSaveDTO dto) {
        orderService.updateStatus(id, dto);
        return Result.success();
    }


    @PutMapping("/no/{orderNo}/status")
    @Operation(summary = "按订单号修改状态（支付回调用）")
    public Result<Void> updateStatusByOrderNo(@PathVariable String orderNo,
                                              @RequestBody @Valid StatusSaveDTO dto) {
        orderService.updateStatusByOrderNo(orderNo, dto);
        return Result.success();
    }


    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException("未登录");
        }
        return authHeader.substring(7);
    }
}
