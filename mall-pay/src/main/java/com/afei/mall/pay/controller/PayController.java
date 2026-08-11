package com.afei.mall.pay.controller;

import com.afei.common.exception.BusinessException;
import com.afei.common.result.Result;
import com.afei.mall.pay.domain.dto.PayCallbackDTO;
import com.afei.mall.pay.domain.dto.PayCreateDTO;
import com.afei.mall.pay.domain.vo.PayCreateVO;
import com.afei.mall.pay.domain.vo.PayStatusVO;
import com.afei.mall.pay.service.PayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/pay")
@RestController
@Tag(name = "支付接口")
@AllArgsConstructor
public class PayController {
    private final PayService payService;

    @PostMapping
    @Operation(summary = "发起支付")
    public Result<PayCreateVO> pay(@RequestHeader("authorization") String authorization,
                                   @RequestBody PayCreateDTO dto) {
        return Result.success(payService.pay(extractToken(authorization), dto));
    }
    @PostMapping("/callback")
    @Operation(summary = "支付回调")
    public Result<Void> callback(@RequestBody @Valid PayCallbackDTO dto) {
        payService.callback(dto);
        return Result.success();
    }
    @GetMapping("/{orderId}")
    @Operation(summary = "查询支付状态")
    public Result<PayStatusVO> status(@RequestHeader("authorization") String authorization,
                                     @PathVariable Long orderId) {
        return Result.success(payService.status(extractToken(authorization), orderId));
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException("未登录");
        }
        return authHeader.substring(7);
    }
}
