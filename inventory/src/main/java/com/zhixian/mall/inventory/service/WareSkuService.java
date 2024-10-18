package com.zhixian.mall.inventory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.inventory.entity.WareSkuEntity;

import java.util.Map;

/**
 * 商品库存
 *
 * @author zhixian
 * @email raininininin@gmail.com
 * @date 2024-09-23 10:46:16
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);
}

