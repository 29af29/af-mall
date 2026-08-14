package com.afei.mall.product.domain.po;

import com.afei.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("category")
public class Category extends BaseEntity {

    private Long parentId;

    private String name;

    private Integer level;  // 1=一级 2=二级 3=三级

    private Integer sort;

    private String icon;

    private Integer status;  // 0=禁用 1=启用
}
