package com.zhixian.mall.inventory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.inventory.entity.WareInfoEntity;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author zhixian
 * @email raininininin@gmail.com
 * @date 2024-09-23 10:46:15
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

