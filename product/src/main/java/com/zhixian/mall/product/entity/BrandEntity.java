package com.zhixian.mall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import com.zhixian.mall.common.validator.annotation.ListValue;
import com.zhixian.mall.common.validator.group.AddGroup;
import com.zhixian.mall.common.validator.group.UpdateGroup;
import com.zhixian.mall.common.validator.group.UpdateStatusGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@NotNull(message = "修改必须指定品牌id", groups = UpdateGroup.class)
	@Null(message = "新增不能指定品牌id", groups = AddGroup.class)
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名必须提交", groups = {AddGroup.class, UpdateGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@NotEmpty(groups = AddGroup.class)
	@URL(message = "logo必须是合法的Url地址", groups = {AddGroup.class, UpdateGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@NotNull(groups = {AddGroup.class, UpdateStatusGroup.class})
	@ListValue(vals = {0, 1}, groups = {AddGroup.class, UpdateStatusGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotEmpty(groups = AddGroup.class)
	@Pattern(regexp = "^[a-zA-Z]$", groups = {AddGroup.class, UpdateGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(groups = AddGroup.class)
	@Min(value = 0, message = "排序必须大于等于0", groups = {AddGroup.class, UpdateGroup.class})
	private Integer sort;

}
