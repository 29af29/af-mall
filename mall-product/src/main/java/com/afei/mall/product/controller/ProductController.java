package com.afei.mall.product.controller;

import com.afei.common.exception.BusinessException;
import com.afei.common.result.PageResult;
import com.afei.common.result.Result;
import com.afei.mall.product.domain.dto.*;
import com.afei.mall.product.domain.po.Spu;
import com.afei.mall.product.domain.vo.BrandVO;
import com.afei.mall.product.domain.vo.SkuVO;
import com.afei.mall.product.domain.vo.SpuVO;
import com.afei.mall.product.service.BrandService;
import com.afei.mall.product.service.SpuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
@Tag(name = "商品管理")
@AllArgsConstructor
public class ProductController {

    private final BrandService brandService;
    private final SpuService spuService;

    @GetMapping("/brand/page")
    @Operation(summary = "品牌分页")
    public Result<PageResult<BrandVO>> brandPage(BrandPageQueryDTO dto) {
        return Result.success(brandService.page(dto));
    }

    @PostMapping("/brand")
    @Operation(summary = "新增品牌")
    public Result<Void> addBrand(@RequestHeader("Authorization") String authHeader,
                                  @RequestBody @Valid BrandSaveDTO dto) {
        brandService.addBrand(extractToken(authHeader), dto);
        return Result.success();
    }
    @PutMapping("/brand/{id}")
    @Operation(summary = "修改品牌")
    public Result<Void> updateBrand(@RequestHeader("Authorization") String authHeader,
                                    @PathVariable Long id,
                                    @RequestBody @Valid BrandSaveDTO dto) {
        brandService.updateBrand(extractToken(authHeader), id, dto);
        return Result.success();
    }

    @DeleteMapping("/brand/{id}")
    @Operation(summary = "删除品牌")
    public Result<Void> deleteBrand(@RequestHeader("Authorization") String authHeader,
                                    @PathVariable Long id) {
        brandService.removeBrand(extractToken(authHeader), id);
        return Result.success();
    }

    @GetMapping("/spu/page")
    @Operation(summary = "商品分页")
    public Result<PageResult<SpuVO>> spuPage(SpuPageQueryDTO dto) {
        return Result.success(spuService.page(dto));
    }

    @GetMapping("/spu/{id}")
    @Operation(summary = "商品详情")
    public Result<SpuVO> spuDetail(@PathVariable Long id) {
        return Result.success(spuService.spuDetail(id));
    }

    @PostMapping("/spu")
    @Operation(summary = "新增商品 SPU（含 SKU）")
    public Result<Void> addSpu(@RequestHeader("Authorization") String authHeader,
                                  @RequestBody @Valid SpuSaveDTO dto) {
        spuService.addSpu(extractToken(authHeader), dto);
        return Result.success();
    }

    @PutMapping("/spu/{id}")
    @Operation(summary = "修改商品")
    public Result<Void> updateSpu(@RequestHeader("Authorization") String authHeader,
                                  @PathVariable Long id,
                                  @RequestBody @Valid SpuSaveDTO dto) {
        spuService.updateSpu(extractToken(authHeader), id, dto);
        return Result.success();
    }

    @PutMapping("/spu/{id}/saleable")
    @Operation(summary = "上架/下架")
    public Result<Void> updateSaleable(@RequestHeader("Authorization") String authHeader,
                                       @PathVariable Long id,
                                       @RequestBody @Valid SaleableDTO dto) {
        spuService.updateSaleable(extractToken(authHeader), id, dto.getSaleable());
        return Result.success();
    }

    @DeleteMapping("/spu/{id}")
    @Operation(summary = "删除商品")
    public Result<Void> deleteSpu(@RequestHeader("Authorization") String authHeader,
                                  @PathVariable Long id) {
        spuService.removeSpu(extractToken(authHeader), id);
        return Result.success();
    }

    @GetMapping("/sku/{id}")
    @Operation(summary = "SKU 详情")
    public Result<SkuVO> skuDetail(@PathVariable Long id) {
        return Result.success(spuService.skuDetail(id));
    }

    @PutMapping("/sku/{id}/stock")
    @Operation(summary = "扣减库存")
    public Result<Void> deductStock(@PathVariable Long id,
                                    @RequestBody @Valid StockDTO dto) {
        spuService.deductStock(id, dto);
        return Result.success();
    }

    @PutMapping("/sku/{id}/stock/restore")
    @Operation(summary = "恢复库存（订单超时关单回补）")
    public Result<Void> restoreStock(@PathVariable Long id,
                                     @RequestBody @Valid StockDTO dto) {
        spuService.restoreStock(id, dto);
        return Result.success();
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException("未登录");
        }
        return authHeader.substring(7);
    }
}
