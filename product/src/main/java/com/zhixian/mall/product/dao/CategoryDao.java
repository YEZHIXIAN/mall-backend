package com.zhixian.mall.product.dao;

import com.zhixian.mall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author zhixian
 * @email raininininin@gmail.com
 * @date 2024-09-24 19:14:56
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
