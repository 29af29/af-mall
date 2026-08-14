package com.afei.common.feign;

import com.afei.common.feign.dto.SkuInfoDTO;
import com.afei.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient("mall-product")
public interface ProductFeignClient {

    @GetMapping("/api/product/sku/{id}")
    Result<SkuInfoDTO> skuDetail(@PathVariable Long id);

    @PutMapping("/api/product/sku/{id}/stock")
    Result<Void> deductStock(@PathVariable Long id, @RequestBody Map<String, Integer> body);
}
