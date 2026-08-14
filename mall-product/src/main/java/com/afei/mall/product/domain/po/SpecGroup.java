package com.afei.mall.product.domain.po;

import com.afei.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("spec_group")
public class SpecGroup extends BaseEntity {

    private Long categoryId;

    private String name;

    private Integer sort;
}
