package com.afei.mall.product.service;

import com.afei.common.result.PageResult;
import com.afei.mall.product.domain.dto.SpuPageQueryDTO;
import com.afei.mall.product.domain.dto.SpuSaveDTO;
import com.afei.mall.product.domain.dto.StockDTO;
import com.afei.mall.product.domain.po.Spu;
import com.afei.mall.product.domain.vo.SkuVO;
import com.afei.mall.product.domain.vo.SpuVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

public interface SpuService extends IService<Spu> {
    PageResult<SpuVO> page(SpuPageQueryDTO dto);

    SpuVO spuDetail(Long id);

    void addSpu(String token, @Valid SpuSaveDTO dto);

    void updateSpu(String token, Long id, @Valid SpuSaveDTO dto);

    void removeSpu(String token, Long id);

    void updateSaleable(String token, Long id, Boolean saleable);

    SkuVO skuDetail(Long id);

    void deductStock(Long skuId, StockDTO dto);
}
