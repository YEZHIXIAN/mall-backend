package com.zhixian.mall.inventory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.common.vo.SkuHasStockVo;
import com.zhixian.mall.inventory.entity.WareSkuEntity;
import com.zhixian.mall.inventory.vo.LockStockResult;
import com.zhixian.mall.inventory.vo.WareSkuLockVo;

import java.util.List;
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

    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    List<LockStockResult> orderLockStock(WareSkuLockVo vo);
}

