package com.afei.mall.product.controller;

import com.afei.common.exception.BusinessException;
import com.afei.common.result.Result;
import com.afei.mall.product.domain.dto.CategorySaveDTO;
import com.afei.mall.product.domain.po.Category;
import com.afei.mall.product.domain.vo.CategoryVO;
import com.afei.mall.product.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@Tag(name = "分类管理")
@AllArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/category/tree")
    @Operation(summary = "获取分类树")
    public Result<List<CategoryVO>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }

    @PostMapping("/category")
    @Operation(summary = "新增分类")
    public Result<Void> addCategory(@RequestHeader("Authorization") String authHeader,
                                     @RequestBody @Valid CategorySaveDTO categorySaveDTO) {
        categoryService.addCategory(extractToken(authHeader), categorySaveDTO);
        return Result.success();
    }
    @PutMapping("/category/{id}")
    @Operation(summary = "修改分类")
    public Result<Void> updateCategory(@RequestHeader("Authorization") String authHeader,
                                       @PathVariable Long id,
                                       @RequestBody @Valid CategorySaveDTO categorySaveDTO) {
        categoryService.updateCategory(extractToken(authHeader), id, categorySaveDTO);
        return Result.success();
    }

    @DeleteMapping("/category/{id}")
    @Operation(summary = "删除分类")
    public Result<Void> deleteCategory(@RequestHeader("Authorization") String authHeader,
                                       @PathVariable Long id) {
        categoryService.deleteCategory(extractToken(authHeader), id);
        return Result.success();
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException("未登录");
        }
        return authHeader.substring(7);
    }
}
