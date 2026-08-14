package com.afei.mall.product.domain.po;

import com.afei.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("brand")
public class Brand extends BaseEntity {

    private String name;

    private String logo;

    private String description;

    private String firstLetter;  // 首字母，用于 A-Z 索引

    private Integer sort;

    private Integer status;  // 0=禁用 1=启用
}