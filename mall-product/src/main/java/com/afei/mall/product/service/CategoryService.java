package com.afei.mall.product.service;

import com.afei.mall.product.domain.dto.CategorySaveDTO;
import com.afei.mall.product.domain.po.Category;
import com.afei.mall.product.domain.vo.CategoryVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

import java.util.List;

public interface CategoryService extends IService<Category> {

    List<CategoryVO> getCategoryTree();

    void addCategory(String role, CategorySaveDTO categorySaveDTO);

    void updateCategory(String role, Long id, @Valid CategorySaveDTO categorySaveDTO);

    void deleteCategory(String role, Long id);
}
