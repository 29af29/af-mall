package com.afei.mall.search.controller;

import com.afei.common.result.Result;
import com.afei.mall.search.service.EsSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员后台接口（ES 维护等）
 * 注：项目暂未做角色鉴权（mall-auth），仅做路径前缀隔离方便后续扩展
 */
@RestController
@RequestMapping("/admin/search")
@Tag(name = "搜索管理（管理员）")
@RequiredArgsConstructor
public class AdminSearchController {

    private final EsSyncService esSyncService;

    @PostMapping("/es/sync-all")
    @Operation(summary = "ES 全量同步（从 MySQL 拉所有上架商品写入 ES）")
    public Result<Integer> syncAll() {
        return Result.success(esSyncService.syncAllFromDb());
    }
}
