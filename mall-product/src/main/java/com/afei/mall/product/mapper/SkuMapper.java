package com.afei.mall.product.mapper;

import com.afei.mall.product.domain.po.Sku;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SkuMapper extends BaseMapper<Sku> {
    void insertBatch(List<Sku> skuList);
}
