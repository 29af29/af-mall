package com.afei.mall.product.service.impl;

import com.afei.common.exception.BusinessException;
import com.afei.common.jwt.JwtUtils;
import com.afei.common.result.PageResult;
import com.afei.mall.product.domain.dto.BrandPageQueryDTO;
import com.afei.mall.product.domain.dto.BrandSaveDTO;
import com.afei.mall.product.domain.po.Brand;
import com.afei.mall.product.domain.po.CategoryBrand;
import com.afei.mall.product.domain.vo.BrandVO;
import com.afei.mall.product.mapper.BrandMapper;
import com.afei.mall.product.mapper.CategoryBrandMapper;
import com.afei.mall.product.service.BrandService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements BrandService {

    private final JwtUtils jwtUtils;
    private final CategoryBrandMapper categoryBrandMapper;

    @Override
    public PageResult<BrandVO> page(BrandPageQueryDTO dto) {
        Page<Brand> page = this.page(dto.toPage(), new LambdaQueryWrapper<Brand>()
                .eq(Brand::getStatus, 1)
                .like(dto.getName() != null && !dto.getName().isEmpty(), Brand::getName, dto.getName())
                .orderByAsc(Brand::getSort));

        List<BrandVO> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(voList, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    public void addBrand(String token, BrandSaveDTO dto) {
        String role = jwtUtils.getRole(token);
        if (!"ADMIN".equals(role)) {
            throw new BusinessException("非管理员角色，不能添加品牌");
        }
        if (this.lambdaQuery().eq(Brand::getName, dto.getName()).count() > 0) {
            throw new BusinessException("品牌名称已存在");
        }
        Brand brand = new Brand();
        BeanUtils.copyProperties(dto, brand);
        brand.setStatus(1);
        this.save(brand);
    }

    @Override
    public void updateBrand(String token, Long id, BrandSaveDTO dto) {
        String role = jwtUtils.getRole(token);
        if (!"ADMIN".equals(role)) {
            throw new BusinessException("非管理员角色，不能更新品牌");
        }
        Brand brand = this.getById(id);
        if (brand == null) {
            throw new BusinessException("品牌不存在");
        }
        if (dto.getName() != null) {
            boolean exists = this.lambdaQuery()
                    .eq(Brand::getName, dto.getName())
                    .ne(Brand::getId, id)
                    .count() > 0;
            if (exists) {
                throw new BusinessException("品牌名称已存在");
            }
        }
        BeanUtils.copyProperties(dto, brand, "id", "status");
        this.updateById(brand);
    }

    @Override
    public void removeBrand(String token, Long id) {
        String role = jwtUtils.getRole(token);
        if (!"ADMIN".equals(role)) {
            throw new BusinessException("非管理员角色，不能删除品牌");
        }
        Brand brand = this.getById(id);
        if (brand == null) {
            throw new BusinessException("品牌不存在");
        }
        // 校验品牌是否已被分类关联
        long count = categoryBrandMapper.selectCount(
                new LambdaQueryWrapper<CategoryBrand>()
                        .eq(CategoryBrand::getBrandId, id));
        if (count > 0) {
            throw new BusinessException("品牌已被分类关联，不能删除");
        }
        this.removeById(id);
    }

    private BrandVO toVO(Brand brand) {
        return BrandVO.builder()
                .id(brand.getId())
                .name(brand.getName())
                .logo(brand.getLogo())
                .description(brand.getDescription())
                .firstLetter(brand.getFirstLetter())
                .sort(brand.getSort())
                .build();
    }
}
