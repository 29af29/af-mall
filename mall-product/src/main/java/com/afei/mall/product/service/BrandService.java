package com.afei.mall.product.service;

import com.afei.common.result.PageResult;
import com.afei.mall.product.domain.dto.BrandPageQueryDTO;
import com.afei.mall.product.domain.dto.BrandSaveDTO;
import com.afei.mall.product.domain.po.Brand;
import com.afei.mall.product.domain.vo.BrandVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

public interface BrandService extends IService<Brand> {

    PageResult<BrandVO> page(BrandPageQueryDTO dto);

    void addBrand(String role, BrandSaveDTO dto);

    void updateBrand(String role, Long id, @Valid BrandSaveDTO dto);

    void removeBrand(String role, Long id);
}
