package com.zhixian.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mysql.cj.util.StringUtils;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.common.utils.Query;
import com.zhixian.mall.product.dao.SkuInfoDao;
import com.zhixian.mall.product.entity.SkuImagesEntity;
import com.zhixian.mall.product.entity.SkuInfoEntity;
import com.zhixian.mall.product.entity.SpuInfoDescEntity;
import com.zhixian.mall.product.service.AttrGroupService;
import com.zhixian.mall.product.service.SkuImagesService;
import com.zhixian.mall.product.service.SkuInfoService;
import com.zhixian.mall.product.service.SpuInfoDescService;
import com.zhixian.mall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService imagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isNullOrEmpty(key) && !"0".equals(key)) {
            wrapper.and((obj) -> obj.eq("sku_id", key).or().like("sku_name", key));
        }

        String catalogId = (String) params.get("catalogId");
        if (!StringUtils.isNullOrEmpty(catalogId) && !"0".equals(catalogId)) {
            wrapper.eq("catalog_id", catalogId);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isNullOrEmpty(brandId) && !"0".equals(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        String min = (String) params.get("min");
        if (!StringUtils.isNullOrEmpty(min)) {
            wrapper.ge("price", min);

        }

        String max = (String) params.get("max");
        if (!StringUtils.isNullOrEmpty(max)) {
            try {
                BigDecimal maxBigDecimal = new BigDecimal(max);
                if (maxBigDecimal.compareTo(BigDecimal.ZERO) > 0) {
                    wrapper.le("price", max);
                }
            } catch (Exception ignored) {

            }
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {

        SkuItemVo skuItemVo = new SkuItemVo();

        // 异步获取 SKU 基本信息
        CompletableFuture<SkuInfoEntity> infoFuture =
                CompletableFuture.supplyAsync(() -> this.getById(skuId));

        // 使用 infoFuture 结果获取 SKU 图片信息
        CompletableFuture<List<SkuImagesEntity>> imagesFuture =
                infoFuture.thenComposeAsync(info -> CompletableFuture.supplyAsync(() -> imagesService.getImagesBySkuId(info.getSkuId())));

        // 使用 infoFuture 结果获取 SPU 信息
        CompletableFuture<SpuInfoDescEntity> spuDescFuture =
                infoFuture.thenComposeAsync(info -> CompletableFuture.supplyAsync(() -> spuInfoDescService.getById(info.getSpuId())));

        // 使用 infoFuture 结果获取 SPU 属性信息

        List<SkuItemVo.SkuItemSaleAttrVo> saleAttrVos = new ArrayList<>();
        List<SkuItemVo.SpuItemAttrGroupVo> groupAttrs = attrGroupService.getAttrGroupWithAttrsBySpuId(infoFuture.get().getSpuId(), saleAttrVos);

        // 等待所有依赖完成并组装结果
        CompletableFuture.allOf(imagesFuture, spuDescFuture).join();

        skuItemVo.setInfo(infoFuture.get());
        skuItemVo.setImages(imagesFuture.get());
        skuItemVo.setDesc(spuDescFuture.get());
        skuItemVo.setGroupAttrs(groupAttrs);
        skuItemVo.setSaleAttr(saleAttrVos);

        return skuItemVo;
    }

}