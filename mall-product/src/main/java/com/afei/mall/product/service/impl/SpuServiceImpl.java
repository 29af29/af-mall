package com.afei.mall.product.service.impl;

import com.afei.common.exception.BusinessException;
import com.afei.common.jwt.JwtUtils;
import com.afei.common.result.PageResult;
import com.afei.mall.product.domain.dto.SkuSaveDTO;
import com.afei.mall.product.domain.dto.SpuPageQueryDTO;
import com.afei.mall.product.domain.dto.SpuSaveDTO;
import com.afei.mall.product.domain.dto.StockDTO;
import com.afei.mall.product.domain.po.Brand;
import com.afei.mall.product.domain.po.Sku;
import com.afei.mall.product.domain.po.Spu;
import com.afei.mall.product.domain.vo.SkuVO;
import com.afei.mall.product.domain.vo.SpuVO;
import com.afei.mall.product.mapper.SkuMapper;
import com.afei.mall.product.mapper.SpuMapper;
import com.afei.mall.product.service.BrandService;
import com.afei.mall.product.service.SpuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SpuServiceImpl extends ServiceImpl<SpuMapper, Spu> implements SpuService {

    private final BrandService brandService;
    private final SkuMapper skuMapper;
    private final JwtUtils jwtUtils;

    @Override
    public PageResult<SpuVO> page(SpuPageQueryDTO dto) {
        Page<Spu> page = this.page(dto.toPage(), new LambdaQueryWrapper<Spu>()
                .eq(dto.getCategory3Id() != null, Spu::getCategory3Id, dto.getCategory3Id())
                .eq(dto.getBrandId() != null, Spu::getBrandId, dto.getBrandId())
                .eq(dto.getSaleable() != null, Spu::getSaleable, Boolean.TRUE.equals(dto.getSaleable()) ? 1 : 0)
                .like(dto.getKey() != null && !dto.getKey().isEmpty(), Spu::getName, dto.getKey())
                .orderByDesc(Spu::getCreateTime));

        List<SpuVO> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(voList, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    public SpuVO spuDetail(Long id) {
        Spu spu = this.getById(id);
        if (spu == null) {
            throw new BusinessException("商品不存在");
        }
        SpuVO spuVO = toVO(spu);
        List<SkuVO> skuVOList = skuMapper.selectList(new LambdaQueryWrapper<Sku>()
                        .eq(Sku::getSpuId, spu.getId()))
                .stream().map(this::toVO).collect(Collectors.toList());
        spuVO.setSkus(skuVOList);
        return spuVO;
    }

    @Transactional
    @Override
    public void addSpu(String token, SpuSaveDTO dto) {
        String role = jwtUtils.getRole(token);
        if (!"ADMIN".equals(role)) {
            throw new BusinessException("非管理员角色，不能新增商品");
        }
        if (this.lambdaQuery().eq(Spu::getName, dto.getName()).count() > 0) {
            throw new BusinessException("商品名称已存在");
        }
        // 新增时必填字段校验
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BusinessException("商品名称不能为空");
        }
        if (dto.getBrandId() == null) {
            throw new BusinessException("品牌ID不能为空");
        }
        if (dto.getCategory3Id() == null) {
            throw new BusinessException("分类ID不能为空");
        }
        // 保存 SPU
        Spu spu = new Spu();
        BeanUtils.copyProperties(dto, spu, "mainImage", "detail", "skus");
        spu.setPics(dto.getMainImage() != null ? dto.getMainImage() : "");
        spu.setDescription(dto.getDetail());
        spu.setSaleable(1);
        this.save(spu);

        // 保存 SKU
        if (dto.getSkus() != null && !dto.getSkus().isEmpty()) {
            for (SkuSaveDTO skuDto : dto.getSkus()) {
                Sku sku = new Sku();
                BeanUtils.copyProperties(skuDto, sku, "ownSpec", "title");
                sku.setSpuId(spu.getId());
                sku.setName(skuDto.getTitle());
                sku.setSpec(skuDto.getOwnSpec() != null ? skuDto.getOwnSpec() : "");
                sku.setStatus(1);
                skuMapper.insert(sku);
            }
        }
    }

    @Transactional
    @Override
    public void updateSpu(String token, Long id, SpuSaveDTO dto) {
        String role = jwtUtils.getRole(token);
        if (!"ADMIN".equals(role)) {
            throw new BusinessException("非管理员角色，不能修改商品");
        }
        Spu spu = this.getById(id);
        if (spu == null) {
            throw new BusinessException("商品不存在");
        }
        // 名称去重（排除自身）
        if (dto.getName() != null) {
            boolean exists = this.lambdaQuery()
                    .eq(Spu::getName, dto.getName())
                    .ne(Spu::getId, id)
                    .count() > 0;
            if (exists) {
                throw new BusinessException("商品名称已存在");
            }
        }
        BeanUtils.copyProperties(dto, spu, "mainImage", "detail", "skus", "saleable");
        if (dto.getMainImage() != null) {
            spu.setPics(dto.getMainImage());
        }
        if (dto.getDetail() != null) {
            spu.setDescription(dto.getDetail());
        }
        this.updateById(spu);

        // 如果传了 SKU，先删旧再加新
        if (dto.getSkus() != null && !dto.getSkus().isEmpty()) {
            skuMapper.delete(new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, id));
            for (SkuSaveDTO skuDto : dto.getSkus()) {
                Sku sku = new Sku();
                BeanUtils.copyProperties(skuDto, sku, "ownSpec", "title");
                sku.setSpuId(id);
                sku.setName(skuDto.getTitle());
                sku.setSpec(skuDto.getOwnSpec() != null ? skuDto.getOwnSpec() : "");
                sku.setStatus(1);
                skuMapper.insert(sku);
            }
        }
    }

    @Transactional
    @Override
    public void removeSpu(String token, Long id) {
        //校验角色
        String role = jwtUtils.getRole(token);
        if (!"ADMIN".equals(role)) {
            throw new BusinessException("非管理员角色，不能删除商品");
        }
        Spu spu = this.getById(id);
        if (spu == null) {
            throw new BusinessException("商品不存在");
        }
        skuMapper.delete(new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, id));
        this.removeById(id);
    }

    @Transactional
    @Override
    public void updateSaleable(String token, Long id, Boolean saleable) {
        String role = jwtUtils.getRole(token);
        if (!"ADMIN".equals(role)) {
            throw new BusinessException("非管理员角色，不能上下架");
        }
        Spu spu = this.getById(id);
        if (spu == null) {
            throw new BusinessException("商品不存在");
        }
        spu.setSaleable(Boolean.TRUE.equals(saleable) ? 1 : 0);
        this.updateById(spu);
        // TODO 上架时通过 MQ 发消息给搜索服务同步 ES
        // rabbitTemplate.convertAndSend("spu.saleable", spu);
    }

    @Override
    public SkuVO skuDetail(Long id) {
        Sku sku = skuMapper.selectById(id);
        if (sku == null) {
            throw new BusinessException("SKU不存在");
        }
        return toVO(sku);
    }

    @Override
    public void deductStock(Long skuId, StockDTO dto) {
        Sku sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException("SKU 不存在");
        }
        if (sku.getStock() < dto.getNum()) {
            throw new BusinessException("商品库存不足");
        }
        sku.setStock(sku.getStock() - dto.getNum());
        skuMapper.updateById(sku);
    }

    private SpuVO toVO(Spu spu) {
        Brand brand = brandService.getById(spu.getBrandId());
        return SpuVO.builder()
                .id(spu.getId())
                .name(spu.getName())
                .caption(spu.getCaption())
                .brandId(spu.getBrandId())
                .brandName(brand != null ? brand.getName() : null)
                .category3Id(spu.getCategory3Id())
                .mainImage(spu.getPics() != null && !spu.getPics().isEmpty()
                        ? spu.getPics().split(",")[0] : null)
                .detail(spu.getDescription())
                .saleable(spu.getSaleable() != null && spu.getSaleable() == 1)
                .build();
    }

    private SkuVO toVO(Sku sku) {
        return SkuVO.builder()
                .id(sku.getId())
                .spuId(sku.getSpuId())
                .title(sku.getName())
                .price(sku.getPrice())
                .stock(sku.getStock())
                .images(sku.getImages())
                .ownSpec(sku.getSpec())
                .enable(sku.getStatus() != null && sku.getStatus() == 1)
                .build();
    }
}
