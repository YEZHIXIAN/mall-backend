package com.zhixian.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.common.utils.Query;
import com.zhixian.mall.product.dao.SkuSaleAttrValueDao;
import com.zhixian.mall.product.entity.SkuSaleAttrValueEntity;
import com.zhixian.mall.product.service.SkuSaleAttrValueService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<String> getSkuSaleAttrValuesAsStringList(Long skuId) {
        List<SkuSaleAttrValueEntity> saleAttrs = this.list(new QueryWrapper<SkuSaleAttrValueEntity>().eq("sku_id", skuId));
        if (saleAttrs != null && !saleAttrs.isEmpty()) {
            return saleAttrs.stream().map(
                    v -> v.getAttrName() + ":" + v.getAttrValue()
            ).collect(Collectors.toList());
        }
        return List.of();
    }

}