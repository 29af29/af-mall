package com.afei.mall.product.service.impl;

import com.afei.common.exception.BusinessException;
import com.afei.common.jwt.JwtUtils;
import com.afei.mall.product.domain.dto.CategorySaveDTO;
import com.afei.mall.product.domain.po.Category;
import com.afei.mall.product.domain.vo.CategoryVO;
import com.afei.mall.product.mapper.CategoryMapper;
import com.afei.mall.product.service.CategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtils jwtUtils;

    private static final String CATEGORY_TREE_KEY = "product:category:tree";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    @Override
    public List<CategoryVO> getCategoryTree() {
        // 1. 先查 Redis 缓存（降级策略：Redis 异常时直接查数据库）
        List<CategoryVO> cached = null;
        try {
            cached = (List<CategoryVO>) redisTemplate.opsForValue().get(CATEGORY_TREE_KEY);
        } catch (Exception e) {
            log.warn("读取分类缓存失败，降级查询数据库", e);
        }
        if (cached != null) {
            return cached;
        }

        // 2. 缓存未命中，查数据库
        List<Category> all = this.lambdaQuery()
                .orderByAsc(Category::getSort)
                .list();
        List<CategoryVO> tree = all.stream()
                .filter(c -> c.getParentId() == 0)
                .map(root -> buildTree(root, all))
                .collect(Collectors.toList());

        // 3. 写回 Redis，30 分钟过期（失败则忽略）
        try {
            redisTemplate.opsForValue().set(CATEGORY_TREE_KEY, tree, CACHE_TTL);
        } catch (Exception e) {
            log.warn("写入分类缓存失败", e);
        }

        return tree;
    }

    @Override
    public void addCategory(String token, CategorySaveDTO categorySaveDTO) {
        // 校验是否是管理员
        String role = jwtUtils.getRole(token);
        if (!"ADMIN".equals(role)) {
            throw new BusinessException("非管理员角色，不能添加分类");
        }
        Category category = new Category();
        BeanUtils.copyProperties(categorySaveDTO, category);
        // 自动计算 level
        if (categorySaveDTO.getParentId() == 0) {
            category.setLevel(1);
        } else {
            Category parent = this.getById(categorySaveDTO.getParentId());
            if (parent == null) {
                throw new BusinessException("父分类不存在");
            }
            category.setLevel(parent.getLevel() + 1);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        this.save(category);
        // 清空缓存
        try {
            redisTemplate.delete(CATEGORY_TREE_KEY);
        } catch (Exception e) {
            log.warn("删除分类缓存失败", e);
        }
    }

    @Override
    public void updateCategory(String token, Long id, CategorySaveDTO categorySaveDTO) {
        // 校验是否是管理员
        String role = jwtUtils.getRole(token);
        if (!"ADMIN".equals(role)) {
            throw new BusinessException("非管理员角色，不能更新分类");
        }
        Category category = this.getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        // 记录旧的 parentId，用于判断是否需要重新计算 level
        Long oldParentId = category.getParentId();
        BeanUtils.copyProperties(categorySaveDTO, category);
        if (!categorySaveDTO.getParentId().equals(oldParentId)) {
            if (categorySaveDTO.getParentId() == 0) {
                category.setLevel(1);
            } else {
                Category parent = this.getById(categorySaveDTO.getParentId());
                if (parent == null) {
                    throw new BusinessException("父分类不存在");
                }
                category.setLevel(parent.getLevel() + 1);
            }
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        this.updateById(category);
        // 清空缓存
        try {
            redisTemplate.delete(CATEGORY_TREE_KEY);
        } catch (Exception e) {
            log.warn("删除分类缓存失败", e);
        }
    }

    @Override
    public void deleteCategory(String token, Long id) {
        // 校验是否是管理员
        String role = jwtUtils.getRole(token);
        if (!"ADMIN".equals(role)) {
            throw new BusinessException("非管理员角色，不能删除分类");
        }
        Category category = this.getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        // 校验是否有子分类
        if (this.lambdaQuery()
                .eq(Category::getParentId, id)
                .count() > 0) {
            throw new BusinessException("分类下有子分类，不能删除");
        }
        this.removeById(id);
        // 清空缓存
        try {
            redisTemplate.delete(CATEGORY_TREE_KEY);
        } catch (Exception e) {
            log.warn("删除分类缓存失败", e);
        }
    }

    private CategoryVO buildTree(Category current, List<Category> all) {
        List<CategoryVO> children = all.stream()
                .filter(c -> c.getParentId().equals(current.getId()))
                .map(child -> buildTree(child, all))
                .collect(Collectors.toList());

        return CategoryVO.builder()
                .id(current.getId())
                .name(current.getName())
                .level(current.getLevel())
                .sort(current.getSort())
                .icon(current.getIcon())
                .children(children)
                .build();
    }
}