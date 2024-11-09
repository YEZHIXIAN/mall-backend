package com.zhixian.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.coupon.entity.UndoLogEntity;

import java.util.Map;

/**
 * 
 *
 * @author zhixian
 * @email raininininin@gmail.com
 * @date 2024-09-24 19:30:59
 */
public interface UndoLogService extends IService<UndoLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

