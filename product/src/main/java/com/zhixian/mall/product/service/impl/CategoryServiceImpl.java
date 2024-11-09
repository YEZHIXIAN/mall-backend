package com.zhixian.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.common.utils.Query;
import com.zhixian.mall.product.dao.CategoryDao;
import com.zhixian.mall.product.entity.CategoryEntity;
import com.zhixian.mall.product.service.CategoryBrandRelationService;
import com.zhixian.mall.product.service.CategoryService;
import com.zhixian.mall.product.vo.Catalog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(new Query<CategoryEntity>().getPage(params), new QueryWrapper<>());

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1. Get all categories
        List<CategoryEntity> entities = baseMapper.selectList(null);

        // 2. Build tree
        return entities.stream().filter(category -> category.getParentCid() == 0).peek(firstLevelCategory -> firstLevelCategory.setChildren(getSubcategories(firstLevelCategory, entities))).sorted(Comparator.comparingInt(CategoryEntity::getSort)).collect(Collectors.toList());
    }

    @Override
    public void removeCategoryByIds(List<Long> list) {
        baseMapper.deleteBatchIds(list);
    }

    /**
     * Recursively get and set subcategories
     *
     * @param root root category
     * @param all  all categories
     * @return subcategories
     */
    private List<CategoryEntity> getSubcategories(CategoryEntity root, List<CategoryEntity> all) {
        return all
                .stream()
                .filter(category -> Objects.equals(category.getParentCid(), root.getCatId()))
                .peek(subcategory -> subcategory.setChildren(getSubcategories(subcategory, all)))
                .sorted(Comparator.comparingInt(CategoryEntity::getSort))
                .collect(Collectors.toList());
    }

    /**
     * 找到catalogId的完整路径
     *
     * @param catalogId 分类id
     * @return 完整路径
     */
    public Long[] findCatalogPath(Long catalogId) {
        List<Long> paths = new ArrayList<>();
        findParentPath(catalogId, paths);
        return paths.toArray(new Long[0]);
    }


    @Caching(evict = {
            @CacheEvict(value = {"category"}, key = "'getLevel1Categories'"),
            @CacheEvict(value = {"category"}, key = "'getCatalogJson'")
    })
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    public void findParentPath(Long catalogId, List<Long> paths) {
        CategoryEntity byId = this.getById(catalogId);
        paths.add(0, byId.getCatId());
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
    }

    /**
     * Get level 1 categories
     * @return level 1 categories
     */
    @Cacheable(value = {"category"}, key = "#root.methodName", sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categories() {
        return this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    /**
     * Get catalog JSON
     * @return catalog JSON
     * @throws InterruptedException exception
     */
    @Cacheable(value = {"category"}, key = "#root.methodName", sync = true)
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() throws InterruptedException {

        Gson gson = new Gson();
        RLock lock = redissonClient.getLock("catalogJson-lock");
        lock.lock();

        Map<String, List<Catalog2Vo>> cataLogMap;
        try {
            // 缓存中没有数据，查询数据库
            List<CategoryEntity> allCategories = this.list(null);
            cataLogMap = getCataLogMap(allCategories);
        } finally {
            // 释放锁
            lock.unlock();
        }

        return cataLogMap;
    }

    private Map<String, List<Catalog2Vo>> getCataLogMap(List<CategoryEntity> allCategories) {
        // 存储二级分类Vo列表，其中每个二级分类Vo包含三级分类列表
        // 查询二级分类
        // 设置二级分类Vo参数
        // 查询三级分类
        // 设置三级分类Vo参数
        // 将三级分类列表设置到二级分类中
        // 设置二级分类Vo参数
        // 查询该二级分类下的三级分类
        // 设置三级分类Vo参数
        // 将三级分类列表设置到二级分类中
        return getCategoriesByParentId(allCategories, 0L)
                .stream()
                .collect(Collectors.toMap(
                        k -> k.getCatId().toString(),

                        // 存储二级分类Vo列表，其中每个二级分类Vo包含三级分类列表
                        v -> {
                            // 查询二级分类
                            List<CategoryEntity> level2Categories = getCategoriesByParentId(allCategories, v.getCatId());
                            if (level2Categories == null) return new ArrayList<>();

                            // 设置二级分类Vo参数
                            // 查询三级分类
                            // 设置三级分类Vo参数
                            // 将三级分类列表设置到二级分类中
                            return level2Categories.stream().map(level2 -> {

                                // 设置二级分类Vo参数
                                Catalog2Vo catalog2Vo = new Catalog2Vo(
                                        v.getCatId().toString(),
                                        null,
                                        level2.getCatId().toString(),
                                        level2.getName()
                                );

                                // 查询该二级分类下的三级分类
                                List<CategoryEntity> level3Categories = getCategoriesByParentId(allCategories, level2.getCatId());
                                if (level3Categories != null) {

                                    // 设置三级分类Vo参数
                                    List<Catalog2Vo.Catalog3Vo> catalog3Vos = level3Categories.stream().map(level3 ->
                                            new Catalog2Vo.Catalog3Vo(
                                                    level2.getCatId().toString(),
                                                    level3.getCatId().toString(),
                                                    level3.getName()
                                            )
                                    ).collect(Collectors.toList());

                                    // 将三级分类列表设置到二级分类中
                                    catalog2Vo.setCatalog3List(catalog3Vos);
                                }
                                return catalog2Vo;

                            }).collect(Collectors.toList());

                        })
                );
    }

    private List<CategoryEntity> getCategoriesByParentId(List<CategoryEntity> allCategories, Long catId) {
        return allCategories
                .stream()
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(catId))
                .collect(Collectors.toList());
    }
}