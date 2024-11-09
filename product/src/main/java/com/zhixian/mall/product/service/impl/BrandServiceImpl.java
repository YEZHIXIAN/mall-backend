package com.zhixian.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.common.utils.Query;
import com.zhixian.mall.product.dao.BrandDao;
import com.zhixian.mall.product.entity.BrandEntity;
import com.zhixian.mall.product.service.BrandService;
import com.zhixian.mall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        QueryWrapper<BrandEntity> wrapper = new QueryWrapper<>();
        if (!key.isEmpty()) {
            wrapper.eq("brand_id", key).or().like("name", key);
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void updateDetail(BrandEntity brand) {
        // Make sure the consistency of redundant fields in other tables
        this.updateById(brand);
        if (!brand.getName().isEmpty()) {
            // Update related tables
            categoryBrandRelationService.updateBrand(brand.getBrandId(), brand.getName());
        }
    }

    @Cacheable(value = "brand", key = "#root.methodName")
    @Override
    public List<BrandEntity> getBrandsByIds(List<Long> brandIds) {

        return this.list(new QueryWrapper<BrandEntity>().in("brand_id", brandIds));
    }

}