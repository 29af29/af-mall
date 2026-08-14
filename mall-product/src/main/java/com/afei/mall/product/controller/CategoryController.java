package com.afei.mall.product.controller;

import com.afei.common.result.Result;
import com.afei.mall.product.domain.dto.CategorySaveDTO;
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
    public Result<Void> addCategory(@RequestHeader("X-User-Role") String role,
                                     @RequestBody @Valid CategorySaveDTO categorySaveDTO) {
        categoryService.addCategory(role, categorySaveDTO);
        return Result.success();
    }
    @PutMapping("/category/{id}")
    @Operation(summary = "修改分类")
    public Result<Void> updateCategory(@RequestHeader("X-User-Role") String role,
                                       @PathVariable Long id,
                                       @RequestBody @Valid CategorySaveDTO categorySaveDTO) {
        categoryService.updateCategory(role, id, categorySaveDTO);
        return Result.success();
    }

    @DeleteMapping("/category/{id}")
    @Operation(summary = "删除分类")
    public Result<Void> deleteCategory(@RequestHeader("X-User-Role") String role,
                                       @PathVariable Long id) {
        categoryService.deleteCategory(role, id);
        return Result.success();
    }
}
