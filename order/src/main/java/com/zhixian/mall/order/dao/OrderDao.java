package com.zhixian.mall.order.dao;

import com.zhixian.mall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author zhixian
 * @email raininininin@gmail.com
 * @date 2024-09-23 10:37:06
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
