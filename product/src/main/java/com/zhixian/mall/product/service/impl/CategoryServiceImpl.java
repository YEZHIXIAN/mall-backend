package com.zhixian.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.common.utils.Query;
import com.zhixian.mall.product.dao.CategoryDao;
import com.zhixian.mall.product.entity.CategoryEntity;
import com.zhixian.mall.product.service.CategoryBrandRelationService;
import com.zhixian.mall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1. Get all categories
        List<CategoryEntity> entities = baseMapper.selectList(null);

        // 2. Build tree
        return entities.stream()
                .filter(category -> category.getParentCid() == 0)
                .peek(firstLevelCategory -> firstLevelCategory.setChildren(getSubcategories(firstLevelCategory, entities)))
                .sorted(Comparator.comparingInt(CategoryEntity::getSort))
                .collect(Collectors.toList());
    }

    @Override
    public void removeCategoryByIds(List<Long> list) {
        baseMapper.deleteBatchIds(list);
    }

    /**
     * Recursively get and set subcategories
     * @param root
     * @param all
     * @return
     */
    private List<CategoryEntity> getSubcategories(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream()
                .filter(category -> Objects.equals(category.getParentCid(), root.getCatId()))
                .peek(subcategory -> subcategory.setChildren(getSubcategories(subcategory, all)))
                .sorted(Comparator.comparingInt(CategoryEntity::getSort))
                .collect(Collectors.toList());
    }

    /**
     * 找到catelogId的完整路径
     * @param catelogId
     * @return
     */
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        findParentPath(catelogId, paths);
        return paths.toArray(new Long[0]);
    }

    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    public void findParentPath(Long catelogId, List<Long> paths) {
        CategoryEntity byId = this.getById(catelogId);
        paths.add(0, byId.getCatId());
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
    }
}