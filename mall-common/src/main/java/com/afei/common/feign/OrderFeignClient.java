package com.afei.common.feign;

import com.afei.common.feign.dto.OrderInfoDTO;
import com.afei.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient("mall-order")
public interface OrderFeignClient {

    @GetMapping("/api/order/{id}")
    Result<OrderInfoDTO> orderDetail(@PathVariable Long id,
                                     @RequestHeader("authorization") String authorization);

    @PutMapping("/api/order/no/{orderNo}/status")
    Result<Void> updateStatus(@PathVariable String orderNo, @RequestBody Map<String, Integer> body);
}
