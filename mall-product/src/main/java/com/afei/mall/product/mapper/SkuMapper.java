package com.afei.mall.product.mapper;

import com.afei.mall.product.domain.po.Sku;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SkuMapper extends BaseMapper<Sku> {
    void insertBatch(List<Sku> skuList);

    /**
     * 原子扣减库存：只有 stock >= num 才执行扣减，防止超卖
     * @return 影响行数，0 表示库存不足或 SKU 不存在
     */
    @Update("UPDATE sku SET stock = stock - #{num} WHERE id = #{id} AND stock >= #{num} AND is_deleted = 0")
    int deductStock(@Param("id") Long id, @Param("num") Integer num);

    /**
     * 原子回补库存（订单取消/超时关闭时调用）
     */
    @Update("UPDATE sku SET stock = stock + #{num} WHERE id = #{id} AND is_deleted = 0")
    int restoreStock(@Param("id") Long id, @Param("num") Integer num);
}
