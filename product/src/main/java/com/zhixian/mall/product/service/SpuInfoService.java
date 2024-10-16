package com.zhixian.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.product.entity.SpuInfoEntity;
import com.zhixian.mall.product.vo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author zhixian
 * @email raininininin@gmail.com
 * @date 2024-09-24 19:14:56
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo vo);

    void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);
}

