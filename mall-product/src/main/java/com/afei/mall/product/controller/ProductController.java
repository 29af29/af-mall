package com.afei.mall.product.controller;

import com.afei.common.result.PageResult;
import com.afei.common.result.Result;
import com.afei.mall.product.domain.dto.*;
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
    public Result<Void> addBrand(@RequestHeader("X-User-Role") String role,
                                  @RequestBody @Valid BrandSaveDTO dto) {
        brandService.addBrand(role, dto);
        return Result.success();
    }
    @PutMapping("/brand/{id}")
    @Operation(summary = "修改品牌")
    public Result<Void> updateBrand(@RequestHeader("X-User-Role") String role,
                                    @PathVariable Long id,
                                    @RequestBody @Valid BrandSaveDTO dto) {
        brandService.updateBrand(role, id, dto);
        return Result.success();
    }

    @DeleteMapping("/brand/{id}")
    @Operation(summary = "删除品牌")
    public Result<Void> deleteBrand(@RequestHeader("X-User-Role") String role,
                                    @PathVariable Long id) {
        brandService.removeBrand(role, id);
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
    public Result<Void> addSpu(@RequestHeader("X-User-Role") String role,
                                  @RequestBody @Valid SpuSaveDTO dto) {
        spuService.addSpu(role, dto);
        return Result.success();
    }

    @PutMapping("/spu/{id}")
    @Operation(summary = "修改商品")
    public Result<Void> updateSpu(@RequestHeader("X-User-Role") String role,
                                  @PathVariable Long id,
                                  @RequestBody @Valid SpuSaveDTO dto) {
        spuService.updateSpu(role, id, dto);
        return Result.success();
    }

    @PutMapping("/spu/{id}/saleable")
    @Operation(summary = "上架/下架")
    public Result<Void> updateSaleable(@RequestHeader("X-User-Role") String role,
                                       @PathVariable Long id,
                                       @RequestBody @Valid SaleableDTO dto) {
        spuService.updateSaleable(role, id, dto.getSaleable());
        return Result.success();
    }

    @DeleteMapping("/spu/{id}")
    @Operation(summary = "删除商品")
    public Result<Void> deleteSpu(@RequestHeader("X-User-Role") String role,
                                  @PathVariable Long id) {
        spuService.removeSpu(role, id);
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
}
