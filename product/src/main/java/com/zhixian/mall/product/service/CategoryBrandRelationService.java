package com.zhixian.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.common.validator.group.AddGroup;
import com.zhixian.mall.common.validator.group.UpdateGroup;
import com.zhixian.mall.product.entity.BrandEntity;
import com.zhixian.mall.product.entity.CategoryBrandRelationEntity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author zhixian
 * @email raininininin@gmail.com
 * @date 2024-09-24 19:14:56
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    void updateBrand(@NotNull(message = "修改必须指定品牌id", groups = UpdateGroup.class) @Null(message = "新增不能指定品牌id", groups = AddGroup.class) Long brandId, @NotBlank(message = "品牌名必须提交", groups = {AddGroup.class, UpdateGroup.class}) String name);

    void updateCategory(Long catId, String name);

    List<BrandEntity> getBrandsByCatId(Long catId);
}

