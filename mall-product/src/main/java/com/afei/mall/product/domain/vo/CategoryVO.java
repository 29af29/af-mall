package com.afei.mall.product.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategoryVO {
    private Long id;
    private String name;
    private Integer level;
    private Integer sort;
    private String icon;
    private List<CategoryVO> children;  // 子分类，用于树形结构
}
