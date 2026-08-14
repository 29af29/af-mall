package com.afei.mall.product.domain.po;

import com.afei.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("spec_param")
public class SpecParam extends BaseEntity {

    private Long groupId;

    private Long categoryId;

    private String name;

    private Integer numeric;   // 0=非数值 1=数值

    private String unit;       // 单位 kg/cm

    private Integer generic;   // 0=否 1=通用属性

    private Integer searching; // 0=否 1=可用于搜索

    private Integer sort;
}
