package com.afei.common.feign;

import com.afei.common.feign.dto.SkuInfoDTO;
import com.afei.common.feign.dto.SpuListItemDTO;
import com.afei.common.result.PageResult;
import com.afei.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("mall-product")
public interface ProductFeignClient {

    @GetMapping("/api/product/sku/{id}")
    Result<SkuInfoDTO> skuDetail(@PathVariable Long id);

    @PutMapping("/internal/product/sku/{id}/stock")
    Result<Void> deductStock(@PathVariable Long id, @RequestBody Map<String, Integer> body);

    @PutMapping("/internal/product/sku/{id}/stock/restore")
    Result<Void> restoreStock(@PathVariable Long id, @RequestBody Map<String, Integer> body);

    /**
     * 分页查询 SPU（用于 ES 全量同步等场景）
     */
    @GetMapping("/api/product/spu/page")
    Result<PageResult<SpuListItemDTO>> spuPage(@RequestParam(required = false) Integer pageNum,
                                               @RequestParam(required = false) Integer pageSize,
                                               @RequestParam(required = false) Boolean saleable);
}
