package com.afei.mall.search.controller;

import com.afei.common.result.PageResult;
import com.afei.common.result.Result;
import com.afei.mall.search.domain.dto.SearchDTO;
import com.afei.mall.search.domain.vo.SearchVO;
import com.afei.mall.search.service.SpuSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/search")
@RestController
@Tag(name = "商品搜索接口")
@RequiredArgsConstructor
public class SearchController {

    private final SpuSearchService spuSearchService;

    @GetMapping("/spu")
    @Operation(summary = "搜索商品（关键词 + 品牌 + 价格区间）")
    public Result<PageResult<SearchVO>> search(SearchDTO dto) {
        return Result.success(spuSearchService.search(dto));
    }

    @GetMapping("/suggest")
    @Operation(summary = "搜索建议（搜索框自动补全）")
    public Result<List<String>> suggest(@RequestParam String prefix) {
        return Result.success(spuSearchService.suggest(prefix));
    }
}
