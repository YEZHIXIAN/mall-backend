package com.zhixian.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.coupon.entity.HomeSubjectEntity;

import java.util.Map;

/**
 * 首页专题表【jd首页下面很多专题，每个专题链接新的页面，展示专题商品信息】
 *
 * @author zhixian
 * @email raininininin@gmail.com
 * @date 2024-09-24 19:30:59
 */
public interface HomeSubjectService extends IService<HomeSubjectEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

