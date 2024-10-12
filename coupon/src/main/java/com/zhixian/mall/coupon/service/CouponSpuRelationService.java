package com.zhixian.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.coupon.entity.CouponSpuRelationEntity;

import java.util.Map;

/**
 * 优惠券与产品关联
 *
 * @author zhixian
 * @email raininininin@gmail.com
 * @date 2024-09-24 19:30:59
 */
public interface CouponSpuRelationService extends IService<CouponSpuRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);
}
