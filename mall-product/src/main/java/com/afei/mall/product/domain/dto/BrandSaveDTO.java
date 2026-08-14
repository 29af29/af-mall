package com.afei.mall.product.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BrandSaveDTO {

    @NotBlank(message = "品牌名称不能为空")
    @Size(max = 50, message = "品牌名称最长50个字符")
    private String name;

    private String logo;

    @Size(max = 500, message = "品牌描述最长500个字符")
    private String description;

    private String firstLetter;

    private Integer sort;
}
