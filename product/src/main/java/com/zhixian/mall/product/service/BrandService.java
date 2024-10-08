package com.zhixian.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.product.entity.BrandEntity;

import java.util.Map;

/**
 * 品牌
 *
 * @author zhixian
 * @email raininininin@gmail.com
 * @date 2024-09-24 19:14:56
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void updateDetail(BrandEntity brand);
}

