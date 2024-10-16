package com.zhixian.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.product.entity.SpuImagesEntity;

import java.util.List;
import java.util.Map;

/**
 * spu图片
 *
 * @author zhixian
 * @email raininininin@gmail.com
 * @date 2024-09-24 19:14:56
 */
public interface SpuImagesService extends IService<SpuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveImages(Long id, List<String> images);
}

