package com.zhixian.mall.inventory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.inventory.entity.PurchaseEntity;

import java.util.Map;

/**
 * 采购信息
 *
 * @author zhixian
 * @email raininininin@gmail.com
 * @date 2024-09-23 10:46:15
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

