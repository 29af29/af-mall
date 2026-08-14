package com.afei.mall.product.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandVO {
    private Long id;
    private String name;
    private String logo;
    private String description;
    private String firstLetter;
    private Integer sort;
}