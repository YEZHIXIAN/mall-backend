package com.zhixian.mall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.common.utils.Query;

import com.zhixian.mall.product.dao.CategoryDao;
import com.zhixian.mall.product.entity.CategoryEntity;
import com.zhixian.mall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
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
                .peek(firstLevelCategory -> firstLevelCategory.setSubcategories(getSubcategories(firstLevelCategory, entities)))
                .sorted(Comparator.comparingInt(CategoryEntity::getSort))
                .collect(Collectors.toList());
    }

    @Override
    public void removeCategoryByIds(List<Long> list) {
        // TODO check whether the to be deleted categories are used elsewhere
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
                .peek(subcategory -> subcategory.setSubcategories(getSubcategories(subcategory, all)))
                .sorted(Comparator.comparingInt(CategoryEntity::getSort))
                .collect(Collectors.toList());
    }

}